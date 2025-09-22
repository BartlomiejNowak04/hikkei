package dev.tfkls.hikkei.provider.auth;

import dev.tfkls.hikkei.model.auth.SessionProvider;
import java.util.OptionalInt;
import java.util.UUID;

public class DummySessionProvider implements SessionProvider {
  @Override
  public UUID createSession(int userId) {
    return UUID.randomUUID();
  }

  @Override
  public OptionalInt getUserBySession(UUID sessionId) {
    return OptionalInt.empty();
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
