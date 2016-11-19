package ctest.lexml;

import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public class Elementos {
    public static boolean isTextoFormatado(EstruturaDocumento.Elemento elemento) {
        return equalsIgnoreCase(elemento.getTipo(), "texto formatado");
    }

    public static boolean isAlteracao(EstruturaDocumento.Elemento elemento) {
        return contains(elemento.getUrn(), "alt");
    }

    public static boolean isTexto(EstruturaDocumento.Elemento elemento) {
        return equalsIgnoreCase(elemento.getIdentificaoRotulo(), "{texto}");
    }
}
