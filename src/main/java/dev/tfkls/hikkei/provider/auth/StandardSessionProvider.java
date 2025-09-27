package dev.tfkls.hikkei.provider.auth;

import dev.tfkls.hikkei.model.auth.SessionProvider;
import dev.tfkls.hikkei.util.ClockView;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class StandardSessionProvider implements SessionProvider {
  private static class Session {
    Session(UUID sessionId, int userId, LocalDateTime expiry) {
      this.sessionId = sessionId;
      this.userId = userId;
      this.expiry = expiry;
    }

    UUID sessionId;
    int userId;
    LocalDateTime expiry;
  }

  public StandardSessionProvider(Duration expiry, ClockView clockView) {
    this.expiry = expiry;
    this.clockView = clockView;

    sessionByUUID = new ConcurrentHashMap<>();
    sessionInvalidations = new ConcurrentHashMap<>();
  }

  private final ClockView clockView;
  private final Duration expiry;
  private final ConcurrentMap<UUID, Session> sessionByUUID;
  private final ConcurrentMap<Integer, LocalDateTime> sessionInvalidations;

  @Override
  public UUID createSession(int userId) {
    Session session =
        new Session(
            // NOTE: maybe pass random via DI as well?
            UUID.randomUUID(), userId, clockView.getCurrentTime().plus(expiry));
    sessionByUUID.put(session.sessionId, session);
    return session.sessionId;
  }

  @Override
  public Optional<Integer> getUserBySession(UUID sessionId) {
    Session session = sessionByUUID.getOrDefault(sessionId, null);
    if (session == null) {
      return Optional.empty();
    } else {
      if (session.expiry.isBefore(clockView.getCurrentTime())
          || session.expiry.isBefore(
              sessionInvalidations.getOrDefault(
                  session.userId, LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)))) {
        sessionByUUID.remove(sessionId);
        return Optional.empty();
      } else {
        return Optional.of(session.userId);
      }
    }
  }

  @Override
  public boolean dropSession(UUID sessionId) {
    return sessionByUUID.remove(sessionId) != null;
  }

  @Override
  public void dropUserSessions(int userId) {
    sessionInvalidations.put(userId, clockView.getCurrentTime().plus(expiry));
  }

  @Override
  public void dropAllSessions() {
    sessionInvalidations.clear();
    sessionByUUID.clear();
  }
}
