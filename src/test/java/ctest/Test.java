package ctest;

import ctest.fixtures.LeisFixture;
import ctest.lexml.EstruturaDocumento;
import ctest.lexml.HierarquiaDocumento;
import ctest.service.NormaParserService;

import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {
        LeisFixture leis = new LeisFixture() { };
        NormaParserService normaParserService = new NormaParserService();

        HierarquiaDocumento hd = normaParserService.parseDocument(leis.conteudoLeiFile("LeiNumeroLetra.txt"));
        hd.getComponentes().forEach(Test::print);
    }

    public static void print(EstruturaDocumento.Node node) {
        System.out.println("urn: " + node.getElemento().getUrn());
        System.out.println("tipo: " + node.getElemento().getTipo());
        System.out.println("descricao: " + node.getElemento().getDescricao());
        System.out.println("des_rotulo: " + node.getElemento().getIdentificaoRotulo());
        System.out.println("texto: " + node.getElemento().getTexto());
        System.out.println("sigla_tipo: " + node.getElemento().getSiglaTipo());
        System.out.println();
        node.getFilhos().forEach(Test::print);
    }
}
