package dev.tfkls.hikkei.server.routes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import dev.tfkls.hikkei.model.auth.AccessManager;
import dev.tfkls.hikkei.model.blob.BlobDataProvider;
import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.model.file.FileProvider;
import dev.tfkls.hikkei.model.file.VersionProvider;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.provider.page.IdentityPageRenderer;
import dev.tfkls.hikkei.server.MainVerticle;
import dev.tfkls.hikkei.test.IntegrationTest;
import io.restassured.RestAssured;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class RouteHandlerIntegrationTest extends IntegrationTest {
  protected UserDataProvider userProvider;
  protected PageDataProvider pageProvider;
  protected RevisionDataProvider revisionProvider;
  protected CommentDataProvider commentProvider;
  protected CategoryDataProvider categoryProvider;
  protected BlobDataProvider blobProvider;
  protected VersionProvider versionProvider;
  protected FileProvider fileProvider;
  protected AccessManager accessManager;

  @BeforeAll
  void initServer(Vertx vertx, VertxTestContext testContext) {
    Integer port = Integer.valueOf(require("PORT"));
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;

    userProvider = mock();
    pageProvider = mock();
    revisionProvider = mock();
    commentProvider = mock();
    categoryProvider = mock();
    blobProvider = mock();
    versionProvider = mock();
    fileProvider = mock();
    accessManager = mock();

    vertx
        .deployVerticle(
            new MainVerticle(
                userProvider,
                pageProvider,
                revisionProvider,
                commentProvider,
                categoryProvider,
                blobProvider,
                versionProvider,
                fileProvider,
                port,
                URI.create(String.format("%s:%d", RestAssured.baseURI, port)),
                null,
                new IdentityPageRenderer(),
                accessManager))
        .onComplete(testContext.succeedingThenComplete());
  }

  @BeforeEach
  void resetMocks() {
    Mockito.reset(
        userProvider,
        pageProvider,
        revisionProvider,
        commentProvider,
        categoryProvider,
        versionProvider,
        fileProvider);
  }

  protected void setupAccessManager() throws Exception {
    when(accessManager.requireUser(any())).thenReturn(1);
    when(accessManager.fetchUser(any())).thenReturn(Optional.of(1));
    doNothing().when(accessManager).setCategory(any(), anyInt());
    doNothing().when(accessManager).verifyPermission(any(), any());
    when(pageProvider.getById(1)).thenReturn(new PageData(1, "access_required", 1));
  }
}
