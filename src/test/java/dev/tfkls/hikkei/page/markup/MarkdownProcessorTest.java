package dev.tfkls.hikkei.page.markup;

import dev.tfkls.hikkei.page.Processor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// TODO: Add more tests!
public class MarkdownProcessorTest {
  @Test
  public void emptyDocumentIsEmpty() {
    Processor processor = MarkdownProcessorFactory.defaultProcessor();

    var processed = processor.parseAndRender("");
    Assertions.assertTrue(processed.isEmpty());
  }

  @Test
  public void wikiLinksAreCompiled() {
    Processor processor = MarkdownProcessorFactory.defaultProcessor();

    var processed = processor.parseAndRender("[[Hello, World!]]");
    Assertions.assertTrue(processed.toString().contains("</a>"));
  }
}
