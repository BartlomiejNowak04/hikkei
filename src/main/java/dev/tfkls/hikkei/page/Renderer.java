package dev.tfkls.hikkei.page;

public interface Renderer {
  boolean canRender();

  CharSequence render();
}
