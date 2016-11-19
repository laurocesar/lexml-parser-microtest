package ctest.fixtures;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;

public interface LeisFixture {

    default Collection<String> linesLeiFile(String fileName) throws IOException {
        String content = conteudoLeiFile(fileName);
        return Arrays.asList(content.split("\\n"));
    }

    default String conteudoLeiFile(String fileName) throws IOException {
        InputStream file = Thread.currentThread().getContextClassLoader().getResourceAsStream("leis/" + fileName);
        return IOUtils.toString(file, Charset.defaultCharset());
    }
}

