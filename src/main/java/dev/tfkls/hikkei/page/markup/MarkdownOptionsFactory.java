package dev.tfkls.hikkei.page.markup;

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.wikilink.WikiLinkExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.util.List;

public class MarkdownOptionsFactory {
  static final DataHolder defaultOptions =
      new MutableDataSet()
          .set(
              Parser.EXTENSIONS,
              List.of(WikiLinkExtension.create(), StrikethroughExtension.create()))
          .toImmutable();

  // @NotNull
  public static Parser defaultParser() {
    return Parser.builder(defaultOptions).build();
  }

  // @NotNull
  public static HtmlRenderer defaultRenderer() {
    return HtmlRenderer.builder(defaultOptions).build();
  }
}
