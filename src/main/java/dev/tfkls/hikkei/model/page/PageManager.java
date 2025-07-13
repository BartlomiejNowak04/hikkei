package dev.tfkls.hikkei.model.page;

import dev.tfkls.hikkei.model.exceptions.BadHandleException;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.model.version.VersionProvider;
import java.util.List;
import java.util.Optional;

public class PageManager {
  private final PageDataProvider provider;
  private final VersionProvider version;

  public PageManager(PageDataProvider pageDataProvider, VersionProvider versionProvider) {
    this.provider = pageDataProvider;
    this.version = versionProvider;
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
    res.removeIf(rev -> rev.approved() == null || !rev.approved());
    return res;
  }

  public String getCurrentContent(int pageId) {
    List<RevisionData> revs = getApprovedRevisions(pageId);
    if (revs == null || revs.isEmpty()) return null;
    return version.getContent(revs.getFirst().filename());
  }
}
