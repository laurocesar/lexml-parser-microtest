package ctest.lexml;

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.getLast;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.startsWithAny;

public class EstruturaDocumento {

    public static void propagarInformacao(EstruturaDocumento.Node node, String urnPai, String tipoPai, String descricaoPai) {
        node.elemento.registrarUrnPai(urnPai);
        node.elemento.registrarTipo(tipoPai);
        node.elemento.registrarDescricao(descricaoPai);
        node.filhos.forEach(filho -> propagarInformacao(filho, node.elemento.urn, node.elemento.tipo, node.elemento.descricao));
    }

    @Getter
    public static class Node {

        private Elemento elemento;
        private List<Node> filhos = new ArrayList<>();

        public Node(Elemento elemento) {
            this.elemento = elemento;
        }

        public void add(Node filho) {
            filhos.add(filho);
        }
    }

    @Getter @Setter
    public static class Elemento {

        private String urn;
        private String tipo;
        private String texto;
        private String descricao;
        private String identificaoRotulo;

        private static int fuckId = 1;

        public Elemento(String urn, String tipo, String descricao, String identificaoRotulo) {
            this.urn = urn;
            this.tipo = tipo;
            this.descricao = descricao;
            this.identificaoRotulo = identificaoRotulo;
        }

        public Elemento(String urn, String tipo, String descricao, String identificaoRotulo, String texto) {
            this(urn, tipo, descricao, identificaoRotulo);
            this.texto = texto;
        }

        public void registrarUrnPai(String urnPai) {
            if (!startsWithAny(urnPai, "cap", "sec")) {
                String urnSemSecAndCap = stream(urnPai.split("_"))
                    .filter(e -> !startsWithAny(e, "cap", "sec"))
                    .collect(joining("_"));

                urn = Joiner.on("_").skipNulls().join(emptyToNull(urnSemSecAndCap), emptyToNull(urn));
            } else if (isNullOrEmpty(urn)) {
                urn = urnPai;
            }
        }

        public void registrarDescricao(String descricao) {
            if (isNullOrEmpty(this.descricao)) {
                this.descricao = descricao;
            }
        }

        public void registrarTipo(String tipo) {
            if (isNotEmpty(tipo) && Elementos.isTextoFormatado(this)) {
                this.tipo = tipo;
            }
        }

        public void registrarIdentificacaoRotulo() {
            if (Elementos.isAlteracao(this)) {
                this.identificaoRotulo = String.valueOf(fuckId++);
            }
        }

        public boolean hasTexto() {
            return texto != null;
        }

        public void setUrn(String urn) {
            this.urn = asList("{articulacao}", "{texto}").contains(urn) ? "" : urn;
        }

        public String getUrn() {
            return urn.replaceAll("\\}\\{", "_").replaceAll("\\{|\\}|\\[|\\]", "");
        }

        public String getDescricao() {
            if (asList("inciso", "alínea").contains(tipo.toLowerCase())) {
                return Joiner.on(" ").join(tipo, descricao);
            }
            return descricao;
        }

        public String getIdentificaoRotulo() {
            return identificaoRotulo.replace(",", "-");
        }

        public String getSiglaTipo() {
            if (isNullOrEmpty(urn)) return "";
            return getLast(asList(getUrn().split("_"))).substring(0, 3);
        }
    }
}
