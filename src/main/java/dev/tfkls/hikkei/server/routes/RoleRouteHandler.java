package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.role.RoleDataProvider;
import dev.tfkls.hikkei.server.controllers.RoleController;
import io.vertx.core.Vertx;

public class RoleRouteHandler extends RouteHandler {
  public RoleRouteHandler(Vertx vertx, RoleDataProvider roleProvider) {
    super(vertx);
    RoleController controller = new RoleController(roleProvider);

    //    router.post("/").blockingHandler(null);
    //    router.post("/byid").blockingHandler(null);
    //    router.post("/user").blockingHandler(null);
    //
    //    router.put("/create").blockingHandler(null);
    //    router.put("/update/name").blockingHandler(null);
    //
    //    router.delete("/delete").blockingHandler(null);
  }
}
