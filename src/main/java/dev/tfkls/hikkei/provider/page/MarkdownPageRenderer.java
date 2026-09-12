package dev.tfkls.hikkei.provider.page;

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.wikilink.WikiLinkExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import dev.tfkls.hikkei.model.page.PageRenderer;
import java.util.List;

public class MarkdownPageRenderer implements PageRenderer {
  /// Wiki links must resolve against the page URL convention shared by the frontend
  /// (`titleToUrl`) and `PageController.getPageByTitle(fromUrl=true)`: a space in the title is an
  /// underscore in the URL. flexmark's default maps spaces to '-', which no route understands.
  static final DataHolder defaultOptions =
      new MutableDataSet()
          .set(
              Parser.EXTENSIONS,
              List.of(WikiLinkExtension.create(), StrikethroughExtension.create()))
          .set(WikiLinkExtension.LINK_ESCAPE_CHARS, " ")
          .set(WikiLinkExtension.LINK_REPLACE_CHARS, "_")
          .toImmutable();

  public MarkdownPageRenderer() {
    this(defaultOptions);
  }

  public MarkdownPageRenderer(DataHolder markdownOptions) {
    parser = Parser.builder(defaultOptions).build();
    renderer = HtmlRenderer.builder(defaultOptions).build();
  }

  private final Parser parser;
  private final HtmlRenderer renderer;

  @Override
  public String render(String rawData) {
    return renderer.render(parser.parse(BasedSequence.of(rawData)));
  }
}
