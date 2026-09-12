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
  static final DataHolder defaultOptions =
      new MutableDataSet()
          .set(
              Parser.EXTENSIONS,
              List.of(WikiLinkExtension.create(), StrikethroughExtension.create()))
          .toImmutable();

  public MarkdownPageRenderer() {
    this(defaultOptions);
  }

  public MarkdownPageRenderer(DataHolder markdownOptions) {
    parser = Parser.builder(markdownOptions).build();
    renderer = HtmlRenderer.builder(markdownOptions).build();
  }

  private final Parser parser;
  private final HtmlRenderer renderer;

  @Override
  public String render(String rawData) {
    return renderer.render(parser.parse(BasedSequence.of(rawData)));
  }
}
