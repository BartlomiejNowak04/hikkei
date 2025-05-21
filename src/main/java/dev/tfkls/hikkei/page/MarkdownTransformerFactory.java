package dev.tfkls.hikkei.page;

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.wikilink.WikiLinkExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.util.List;

public class MarkdownTransformerFactory {
  static final DataHolder defaultOptions =
      new MutableDataSet()
          .set(
              Parser.EXTENSIONS,
              List.of(WikiLinkExtension.create(), StrikethroughExtension.create()))
          .toImmutable();

  public static MarkdownTransformer defaultTransformer() {
    return new MarkdownTransformer(
        Parser.builder(defaultOptions)::build, HtmlRenderer.builder(defaultOptions)::build);
  }
}
