package dev.tfkls.hikkei.model.page;

import dev.tfkls.hikkei.model.exceptions.BadHandleException;
import java.util.List;
import java.util.Optional;

public class PageManager {
  private final PageDataProvider provider;

  public PageManager(PageDataProvider pageDataProvider) {
    this.provider = pageDataProvider;
  }

  Optional<PageHandle> getPageById(int pageId) {
    return provider.doesPageExist(pageId) ? Optional.of(new PageHandle(pageId)) : Optional.empty();
  }

  public PageData getRandomPage() {
    Integer id = provider.getRandomPage();
    if (id == null) return null;
    try {
      return provider.getPageData(id);
    } catch (BadHandleException e) {
      return null;
    }
  }

  public List<PageData> searchByTitle(String title) {
    return provider.searchPageByTitle(title);
  }
}
