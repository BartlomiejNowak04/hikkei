package dev.tfkls.hikkei.page;

public interface Processor extends Parser, Renderer {
  default CharSequence parseAndRender(CharSequence input) {
    parse(input);
    assert canRender();
    return render();
  }
}
