package dev.tfkls.hikkei;

import dev.tfkls.hikkei.server.MainVerticle;
import io.vertx.core.Vertx;

public class Main {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }
}
