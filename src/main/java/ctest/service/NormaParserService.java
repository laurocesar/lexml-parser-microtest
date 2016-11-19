package ctest.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kiama.attribution.Attributable;

import br.gov.lexml.parser.pl.block.HasId$;
import br.gov.lexml.symbolicobject.impl.Documento;
import br.gov.lexml.symbolicobject.impl.GenderName;
import br.gov.lexml.symbolicobject.impl.ObjetoSimbolico;
import br.gov.lexml.symbolicobject.impl.ObjetoSimbolicoComplexo;
import br.gov.lexml.symbolicobject.impl.Posicao;
import br.gov.lexml.symbolicobject.impl.Rotulo;
import br.gov.lexml.symbolicobject.impl.Rotulo$;
import br.gov.lexml.symbolicobject.impl.TextoFormatado;
import br.gov.lexml.symbolicobject.parser.IdSource;
import br.gov.lexml.symbolicobject.parser.InputDocument;
import br.gov.lexml.symbolicobject.parser.Paragrafos;
import br.gov.lexml.symbolicobject.parser.Parser;
import br.gov.lexml.symbolicobject.tipos.STipo;
import br.gov.lexml.symbolicobject.tipos.Tipos;
import ctest.lexml.EstruturaDocumento;
import ctest.lexml.HierarquiaDocumento;
import scala.Option;
import scala.runtime.BoxedUnit;
import scalaz.Validation;

public class NormaParserService {

    private Parser lexmlParser = new Parser(new IdSource() {
        private int currentId = 0;
        @Override
        public long nextId(STipo type) {
            int nextId = currentId;
            currentId++;
            return nextId;
        }
    });

    public HierarquiaDocumento parseDocument(String docContent) {
        InputDocument inputDocument = toInputDocument(docContent);
        Validation<Throwable, Documento<BoxedUnit>> result = lexmlParser.parse(inputDocument);
        Documento<BoxedUnit> outputDocument = toOutputDocument(result);

        outputDocument.os().initTreeProperties();
        scala.collection.Iterator<ObjetoSimbolico<BoxedUnit>> iterator = outputDocument.getObjetoSimbolico().toStream().toIterator();
        ObjetoSimbolico<BoxedUnit> rootLexml = iterator.next();

        return criarDocumento(rootLexml).propagarInformacaoParaFilhos();
    }

    private InputDocument toInputDocument(String docContent) {
        Collection<String> lines = Arrays.asList(docContent.split("\\n"));
        Paragrafos paragrafos = new Paragrafos(lines);
        return new InputDocument(Tipos.DocProjetoLei(), paragrafos, "");
    }

    private Documento<BoxedUnit> toOutputDocument(Validation<Throwable, Documento<BoxedUnit>> result) {
        if (result.isFailure()) throw new RuntimeException(result.toEither().left().get());

        Documento<BoxedUnit> document = result.valueOr(null);
        if (document == null) throw new RuntimeException("documento invalido");

        return document;
    }

    private HierarquiaDocumento criarDocumento(ObjetoSimbolico<BoxedUnit> rootLexml) {
        HierarquiaDocumento documento = new HierarquiaDocumento();
        scala.collection.Iterator<Attributable> elementosLexml = rootLexml.children();

        while (elementosLexml.hasNext()) {
            EstruturaDocumento.Node filho = criarEstruturaDocumento(elementosLexml.next());
            documento.add(filho);
        }

        return documento;
    }

    private EstruturaDocumento.Node criarEstruturaDocumento(Attributable elementoLexml) {
        if (elementoLexml instanceof Posicao) {
            Posicao posicao = (Posicao) elementoLexml;
            EstruturaDocumento.Node node = criarEstruturaDocumento(elementoLexml.children().next());
            node.getElemento().setUrn(urnFor(posicao.getRotulo()));
            node.getElemento().setDescricao(descricaoFor(posicao.getRotulo()));
            node.getElemento().setIdentificaoRotulo(identicacaoFor(posicao.getRotulo()));

            return node;

        } else if (elementoLexml instanceof TextoFormatado) {
            TextoFormatado textoFormatado = (TextoFormatado) elementoLexml;

            EstruturaDocumento.Elemento elemento = new EstruturaDocumento.Elemento(
                "{replace urn}",
                textoFormatado.tipo().descricaoTipo(),
                "{replace descricao}",
                "{replace identicacaoRotulo}",
                textoFormatado.repr()
            );

            return new EstruturaDocumento.Node(elemento);

        } else if (elementoLexml instanceof ObjetoSimbolicoComplexo) {
            ObjetoSimbolicoComplexo objeto = (ObjetoSimbolicoComplexo) elementoLexml;

            EstruturaDocumento.Node nodePai = new EstruturaDocumento.Node(
                new EstruturaDocumento.Elemento(
                    "{replace urn}",
                    objeto.tipo().descricaoTipo(),
                    "{replace descricao}",
                    "{replace identicacaoRotulo}"
                )
            );

            objeto.getPosicoes().forEach(posicao -> {
                EstruturaDocumento.Node nodeFilho = criarEstruturaDocumento((Attributable) posicao);
                nodePai.add(nodeFilho);
            });

            return nodePai;

        } else {
            throw new RuntimeException("documento nao reconhecido: " + elementoLexml);
        }
    }

    private String urnFor(Rotulo rotulo) {
        Option<br.gov.lexml.parser.pl.rotulo.Rotulo> rotuloOption = Rotulo$.MODULE$.toRotuloLexml(rotulo);
        if (rotuloOption.isDefined()) {
            return HasId$.MODULE$.renderId(rotuloOption.get());
        }
        return rotulo.repr();
    }

    private String descricaoFor(Rotulo rotulo) {
        Option<GenderName> descricao = Rotulo$.MODULE$.render(rotulo);
        return descricao.isDefined() ? descricao.get().toString() : "";
    }

    private Pattern RotuloPattern = Pattern.compile("\\{.*\\[(.+)\\]\\}");

    private String identicacaoFor(Rotulo rotulo) {
        String labelRotulo = rotulo.repr();
        if (labelRotulo.contains("par;unico")) return "1";

        Matcher matcher = RotuloPattern.matcher(labelRotulo);
        return matcher.find() ? matcher.replaceAll("$1") : "0";
    }
}
