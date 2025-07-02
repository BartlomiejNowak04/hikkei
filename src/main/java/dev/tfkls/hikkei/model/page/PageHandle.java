package dev.tfkls.hikkei.model.page;

public class PageHandle {
  private final int id;

  protected PageHandle(int id) {
    this.id = id;
  }

  protected int getId() {
    return id;
  }
}
