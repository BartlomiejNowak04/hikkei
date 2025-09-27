package dev.tfkls.hikkei.model.auth;

import java.util.Optional;
import java.util.UUID;

public interface SessionProvider {
  /// Register a new session with an Authorization provider
  /// @param userId Identifier of user for which to create a session.
  /// @return The new session identifier.
  UUID createSession(int userId);

  /// Get the data of a user based on the session ID.
  ///
  /// @param sessionId Identifier of a session to authorize.
  /// @return The userId of a user, or empty Optional if id is invalid/expired.
  Optional<Integer> getUserBySession(UUID sessionId);

  /// Drop a given session. Returns true if a session was valid before, false otherwise.
  boolean dropSession(UUID sessionId);

  /// Drop all sessions of a given user.
  void dropUserSessions(int userId);

  /// Drop all sessions of all users.
  void dropAllSessions();
}
