package dev.tfkls.hikkei.server.controllers;

import dev.tfkls.hikkei.model.category.CategoryDataProvider;

public class CategoryController {
  private final CategoryDataProvider categoryProvider;

  public CategoryController(CategoryDataProvider categoryProvider) {
    this.categoryProvider = categoryProvider;
  }
}
