package dev.tfkls.hikkei.model;

/// An externally opaque data type representing a user instance,
///   primarily used within the context of some UserManager class.
public class UserHandle {
  private final int id;
  protected UserHandle(int id) {
    this.id = id;
  }
  /// Package-visible getter for the opaque user identifier
  protected int getId() { return id; }
}
