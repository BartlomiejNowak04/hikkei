package dev.tfkls.hikkei.model.page;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import java.util.List;

public interface PageDataProvider {

  // check existence
  // NOTE: do we actually need it?
  boolean doesPageExist(int pageId) throws InternalErrorException;

  PageData getRandomPage() throws InternalErrorException;

  List<PageData> searchByTitle(String title) throws InternalErrorException;

  PageData getById(int pageId) throws InternalErrorException;

  PageData getByTitle(String title) throws InternalErrorException;

  List<PageData> getByCategory(int categoryId) throws InternalErrorException;

  int createPage(PageData data) throws BadDataException, InternalErrorException;

  void updatePageTitle(int pageId, String title) throws BadDataException, InternalErrorException;

  void updatePageCategory(int pageId, int categoryId)
      throws BadDataException, InternalErrorException;

  void toggleArchive(int pageId, boolean type) throws BadDataException, InternalErrorException;

  void deletePage(int pageId) throws InternalErrorException;
}
