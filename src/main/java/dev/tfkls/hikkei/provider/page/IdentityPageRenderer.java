package dev.tfkls.hikkei.provider.page;

import dev.tfkls.hikkei.model.page.PageRenderer;

public class IdentityPageRenderer implements PageRenderer {
  @Override
  public String render(String rawData) {
    return rawData;
  }
}
