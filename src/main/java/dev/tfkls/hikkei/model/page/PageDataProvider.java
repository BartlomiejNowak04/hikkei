package dev.tfkls.hikkei.model.page;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.revision.RevisionData;
import java.util.List;

public interface PageDataProvider {

  // check existence
  boolean doesPageExist(int pageId) throws InternalErrorException;

  PageData getRandomPage() throws InternalErrorException;

  List<PageData> searchPageByTitle(String title) throws InternalErrorException;

  PageData getPageData(int pageId) throws InternalErrorException;

  PageData getPageByTitle(String title) throws InternalErrorException;

  Integer createPage(PageData data) throws BadDataException, InternalErrorException;

  void updatePageTitle(int pageId, String title) throws BadDataException, InternalErrorException;

  void updatePageCategory(int pageId, int categoryId)
      throws BadDataException, InternalErrorException;

  void toggleArchive(int pageId, boolean type) throws BadDataException, InternalErrorException;

  List<RevisionData> getPageRevisions(int pageId) throws InternalErrorException;

  List<CommentData> getPageComments(int pageId) throws InternalErrorException;

  Integer addComment(CommentData data) throws BadDataException, InternalErrorException;

  void deleteComment(int pageId, int commentId) throws BadDataException, InternalErrorException;
}
