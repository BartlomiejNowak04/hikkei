package dev.tfkls.hikkei.model.page;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.BadHandleException;
import java.util.List;
import java.util.Optional;

public interface PageDataProvider {

  // check existence
  boolean doesPageExist(int pageId);

  Integer getRandomPage();

  List<PageData> searchPageByTitle(String title);

  // get page data
  PageData getPageData(int pageId) throws BadHandleException;

  Optional<Integer> getPageByTitle(String title);

  Integer createPage(PageData data) throws BadDataException;

  void updatePageUrl(int pageId, String url) throws BadHandleException, BadDataException;

  void updatePageCategory(int pageId, int categoryId) throws BadHandleException;

  void archivePage(int pageId) throws BadHandleException;

  // revisions
  List<RevisionData> getPageRevisions(int pageId) throws BadHandleException;

  Integer createRevision(RevisionData data) throws BadHandleException;

  // comments
  List<CommentData> getPageComments(int pageId) throws BadHandleException;

  Integer addComment(int pageId, String content) throws BadHandleException, BadDataException;

  boolean deleteComment(int commentId) throws BadHandleException;
}
