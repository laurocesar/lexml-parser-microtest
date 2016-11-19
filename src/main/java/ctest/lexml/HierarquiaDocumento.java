package ctest.lexml;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Getter
public class HierarquiaDocumento {

    private List<EstruturaDocumento.Node> componentes = new ArrayList<>();

    public void add(EstruturaDocumento.Node node) {
        componentes.add(node);
    }

    public HierarquiaDocumento propagarInformacaoParaFilhos() {
        componentes.forEach(componente -> propagarInformacao(componente, "", "", ""));
        componentes.forEach(this::removerComponenteTexto);
        componentes.forEach(this::registrarIdentificacaoRotulo);
        return this;
    }

    public HierarquiaDocumento reduzirParaComponentesComTexto() {
        HierarquiaDocumento documento = new HierarquiaDocumento();
        List<EstruturaDocumento.Node> componentesComTexto = new ArrayList<>();
        componentes.forEach(componente -> removerComponenteSemTexto(componente, componentesComTexto));
        documento.componentes = componentesComTexto;
        return documento;
    }

    private void removerComponenteSemTexto(EstruturaDocumento.Node node, List<EstruturaDocumento.Node> componentes) {
        if (node.getElemento().hasTexto()) componentes.add(node);
        node.getFilhos().forEach(filho -> removerComponenteSemTexto(filho, componentes));
    }

    private void removerComponenteTexto(EstruturaDocumento.Node pai) {
        Optional<EstruturaDocumento.Node> filhoParaSerRemovido = pai.getFilhos().stream()
            .filter(filho -> equalsIgnoreCase(filho.getElemento().getUrn(), pai.getElemento().getUrn()))
            .findFirst();

        if (filhoParaSerRemovido.isPresent()) {
            EstruturaDocumento.Node filho = filhoParaSerRemovido.get();
            pai.getElemento().setTexto(filho.getElemento().getTexto());
            pai.getFilhos().remove(filho);
        }

        pai.getFilhos().forEach(this::removerComponenteTexto);
    }

    private void registrarIdentificacaoRotulo(EstruturaDocumento.Node node) {
        node.getElemento().registrarIdentificacaoRotulo();
        node.getFilhos().forEach(this::registrarIdentificacaoRotulo);
    }

    private static void propagarInformacao(EstruturaDocumento.Node node, String urnPai, String tipoPai, String descricaoPai) {
        node.getElemento().registrarUrnPai(urnPai);
        node.getElemento().registrarTipo(tipoPai);
        node.getElemento().registrarDescricao(descricaoPai);
        node.getFilhos().forEach(filho ->
            propagarInformacao(
                filho,
                node.getElemento().getUrn(),
                node.getElemento().getTipo(),
                node.getElemento().getDescricao()
            )
        );
    }
}
