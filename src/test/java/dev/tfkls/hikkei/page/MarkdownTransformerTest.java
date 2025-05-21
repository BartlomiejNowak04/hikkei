package dev.tfkls.hikkei.page;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// TODO: Add more tests!
public class MarkdownTransformerTest {
  @Test
  public void emptyDocumentIsEmpty() {
    Transformer processor = MarkdownTransformerFactory.defaultTransformer();

    var processed = processor.transform("");
    Assertions.assertTrue(processed.isEmpty());
  }

  @Test
  public void wikiLinksAreCompiled() {
    Transformer processor = MarkdownTransformerFactory.defaultTransformer();

    var processed = processor.transform("[[Hello, World!]]");
    Assertions.assertTrue(processed.toString().contains("</a>"));
  }
}
