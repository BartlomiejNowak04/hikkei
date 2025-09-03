package dev.tfkls.hikkei.model.category;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import java.util.List;

public interface CategoryDataProvider {
  int createCategory(CategoryData data) throws InternalErrorException, BadDataException;

  List<CategoryData> getCategories() throws InternalErrorException;

  CategoryData getById(int id) throws InternalErrorException;

  void changeName(int id, String name) throws InternalErrorException, BadDataException;

  void updatePermission(int categoryId, int roleId, String permission, Boolean set)
      throws InternalErrorException, BadDataException;
}
