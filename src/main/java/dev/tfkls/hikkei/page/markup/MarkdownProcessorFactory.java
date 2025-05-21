package dev.tfkls.hikkei.page.markup;

public class MarkdownProcessorFactory {
  static MarkdownProcessor defaultProcessor() {
    return new MarkdownProcessor(
        MarkdownOptionsFactory::defaultParser, MarkdownOptionsFactory::defaultRenderer);
  }
}
