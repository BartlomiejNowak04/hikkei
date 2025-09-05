package dev.tfkls.hikkei.model.category;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import java.util.List;
import java.util.Map;

public interface CategoryDataProvider {
  int createCategory(CategoryData data) throws InternalErrorException, BadDataException;

  List<CategoryData> getCategories() throws InternalErrorException;

  CategoryData getById(int id) throws InternalErrorException;

  void changeName(int id, String name) throws InternalErrorException, BadDataException;

  void updatePermission(int categoryId, int userId, PermissionLevel level)
      throws InternalErrorException, BadDataException;

  void deleteCategory(int id) throws InternalErrorException;

  Map<Integer, PermissionLevel> getUsersPermissions(int categoryId) throws InternalErrorException;
}
