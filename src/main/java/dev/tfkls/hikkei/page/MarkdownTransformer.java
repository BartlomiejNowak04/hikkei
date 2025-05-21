package dev.tfkls.hikkei.page;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import java.util.function.Supplier;

public class MarkdownTransformer implements Transformer {
  Parser parser;
  HtmlRenderer renderer;

  public MarkdownTransformer(Supplier<Parser> parser, Supplier<HtmlRenderer> renderer) {
    this.parser = parser.get();
    this.renderer = renderer.get();
  }

  @Override
  public CharSequence transform(CharSequence input) {
    return renderer.render(parser.parse(BasedSequence.of(input)));
  }
}
