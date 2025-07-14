package dev.tfkls.hikkei.model.page;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.BadHandleException;
import dev.tfkls.hikkei.model.revision.RevisionData;
import java.util.List;

public interface PageDataProvider {

  // check existence
  boolean doesPageExist(int pageId);

  Integer getRandomPage();

  List<PageData> searchPageByTitle(String title);

  // get page data
  PageData getPageData(int pageId) throws BadHandleException;

  PageData getPageByTitle(String title);

  Integer createPage(PageData data) throws BadDataException;

  void updatePageUrl(int pageId, String url) throws BadHandleException, BadDataException;

  void updatePageCategory(int pageId, int categoryId) throws BadHandleException;

  void archivePage(int pageId) throws BadHandleException;

  // revisions
  List<RevisionData> getPageRevisions(int pageId) throws BadHandleException;

  // comments
  List<CommentData> getPageComments(int pageId) throws BadHandleException;

  Integer addComment(CommentData data) throws BadHandleException, BadDataException;

  boolean deleteComment(int commentId) throws BadHandleException;
}
