package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.tfkls.hikkei.model.blob.BlobData;
import dev.tfkls.hikkei.model.file.FileProvider;
import dev.tfkls.hikkei.model.file.StoredFile;
import dev.tfkls.hikkei.provider.JDBC.JDBCBlobDataProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCBlobDataProviderIntegrationTest extends JDBCIntegrationTest {
  private JDBCBlobDataProvider provider;
  private FileProvider fileProvider;

  @BeforeAll
  public void initProvider() throws Exception {
    fileProvider = mock();
    provider = new JDBCBlobDataProvider(connection, fileProvider);
  }

  @Test
  void createBlobAndFetchSuccess() throws Exception {
    int user_id = create_user("blob_cbs", null);

    byte[] data = "example_data".getBytes();
    byte[] file = "example_file_identifier".getBytes();

    when(fileProvider.storeContent(data, "application/pdf"))
        .thenReturn("example_file_identifier".getBytes());

    int blob_id = provider.createBlob(data, "application/pdf", user_id);

    BlobData blobData = provider.getBlobById(blob_id);

    assertArrayEquals(file, blobData.file());
    assertEquals(data.length, blobData.size());
    assertEquals(user_id, blobData.userId());
    assertEquals("application/pdf", blobData.mime());
  }

  @Test
  void getBlobDataSuccess() throws Exception {
    int blob_id = create_blob("blob_bds".getBytes());
    byte[] data = "blob_data_example_123".getBytes();

    when(fileProvider.getContent("blob_bds".getBytes()))
        .thenReturn(new StoredFile(data, "application/pdf"));

    byte[] returned = provider.getBlobData(blob_id);

    assertArrayEquals(data, returned);
  }

  @Test
  void deleteBlobSuccess() throws Exception {
    int blob_id = create_blob(new byte[] {0});

    assertNotNull(provider.getBlobById(blob_id));

    provider.deleteBlob(blob_id);

    assertEquals(BlobData.DELETED, provider.getBlobById(blob_id));
  }

  @Test
  void getUserBlobsSuccess() throws Exception {
    int user_id = create_user("blob_gubs", null);

    int[] blobs =
        new int[] {
          create_blob("gubs1".getBytes(), user_id),
          create_blob("gubs2".getBytes(), user_id),
          create_blob("gubs3".getBytes(), user_id)
        };

    var returned = provider.getUserBlobs(user_id);
    assertArrayEquals(blobs, returned.stream().mapToInt(BlobData::id).toArray());
  }
}
