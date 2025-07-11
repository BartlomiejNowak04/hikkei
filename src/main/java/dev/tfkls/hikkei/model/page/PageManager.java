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

  public List<RevisionData> getRevisions(int pageId) {
    try {
      return provider.getPageRevisions(pageId);
    } catch (BadHandleException e) {
      return null;
    }
  }

  public List<RevisionData> getApprovedRevisions(int pageId) {
    List<RevisionData> res = getRevisions(pageId);
    if (res == null) return null;
    res.removeIf(rev -> !rev.approved());
    return res;
  }
}
