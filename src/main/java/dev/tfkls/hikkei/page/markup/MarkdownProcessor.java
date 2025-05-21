package dev.tfkls.hikkei.page.markup;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import dev.tfkls.hikkei.page.Processor;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public class MarkdownProcessor implements Processor {
  Parser parser;
  HtmlRenderer renderer;
  Document syntaxTree;

  public MarkdownProcessor(Supplier<Parser> parser, Supplier<HtmlRenderer> renderer) {
    this.parser = parser.get();
    this.renderer = renderer.get();
    syntaxTree = null;
  }

  @Override
  public void parse(CharSequence input) {
    parse(BasedSequence.of(input));
  }

  public void parse(BasedSequence input) {
    syntaxTree = parser.parse(input);
  }

  @Override
  public boolean canRender() {
    return syntaxTree != null;
  }

  @Override
  @NotNull
  public CharSequence render() {
    var sequence = renderer.render(syntaxTree);
    syntaxTree = null;
    return sequence;
  }
}
