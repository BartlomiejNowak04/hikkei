package dev.tfkls.hikkei.provider.page;

import static org.junit.jupiter.api.Assertions.*;

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class MarkdownPageRendererTest {
  @Test
  void rendersBasicMarkdown() {
    String html = new MarkdownPageRenderer().render("# Title\n\nSome *text*.");
    assertTrue(html.contains("<h1>Title</h1>"), html);
    assertTrue(html.contains("<em>text</em>"), html);
  }

  @Test
  void defaultOptionsEnableWikiLinksAndStrikethrough() {
    String html = new MarkdownPageRenderer().render("See [[Other Page]] and ~~gone~~.");
    assertTrue(html.contains("<a href=\"Other-Page\">Other Page</a>"), html);
    assertTrue(html.contains("<del>gone</del>"), html);
  }

  @Test
  void constructorOptionsAreHonoured() {
    // Only strikethrough: wiki links must stay literal, strikethrough must still render.
    var options =
        new MutableDataSet()
            .set(Parser.EXTENSIONS, List.of(StrikethroughExtension.create()))
            .toImmutable();

    String html = new MarkdownPageRenderer(options).render("See [[Other Page]] and ~~gone~~.");

    assertTrue(html.contains("[[Other Page]]"), html);
    assertFalse(html.contains("<a href"), html);
    assertTrue(html.contains("<del>gone</del>"), html);
  }

  @Test
  void rendererOptionsAreHonoured() {
    var options =
        new MutableDataSet()
            .set(HtmlRenderer.SOFT_BREAK, "<br />\n")
            .set(Parser.EXTENSIONS, List.of())
            .toImmutable();

    String html = new MarkdownPageRenderer(options).render("line one\nline two");

    assertTrue(html.contains("line one<br />\nline two"), html);
  }
}
