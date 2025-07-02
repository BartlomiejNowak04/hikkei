package dev.tfkls.hikkei.model.page;

import java.util.Optional;

public class PageManager {
  private final PageDataProvider provider;

  public PageManager(PageDataProvider pageDataProvider) {
    this.provider = pageDataProvider;
  }

  Optional<PageHandle> getPageById(int pageId) {
    return provider.doesPageExist(pageId) ? Optional.of(new PageHandle(pageId)) : Optional.empty();
  }
}
