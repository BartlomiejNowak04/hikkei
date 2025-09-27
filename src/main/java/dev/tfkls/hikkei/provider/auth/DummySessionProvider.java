package dev.tfkls.hikkei.provider.auth;

import dev.tfkls.hikkei.model.auth.SessionProvider;
import java.util.Optional;
import java.util.UUID;

public class DummySessionProvider implements SessionProvider {
  @Override
  public UUID createSession(int userId) {
    return UUID.randomUUID();
  }

  @Override
  public Optional<Integer> getUserBySession(UUID sessionId) {
    return Optional.empty();
  }

  @Override
  public boolean dropSession(UUID sessionId) {
    return false;
  }

  @Override
  public void dropUserSessions(int userId) {}

  @Override
  public void dropAllSessions() {}
}
