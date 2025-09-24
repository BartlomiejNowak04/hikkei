package dev.tfkls.hikkei.provider.JDBC;

import static dev.tfkls.hikkei.provider.JDBC.Config.*;

import dev.tfkls.hikkei.model.blob.BlobData;
import dev.tfkls.hikkei.model.blob.BlobDataProvider;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.file.FileProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JDBCBlobDataProvider implements BlobDataProvider {
  private final Connection connection;
  private final FileProvider fileProvider;

  public JDBCBlobDataProvider(Connection connection, FileProvider fileProvider) {
    this.connection = connection;
    this.fileProvider = fileProvider;
  }

  @Override
  public int createBlob(byte[] data, String mime, int userId) throws InternalErrorException {
    byte[] filename = fileProvider.storeContent(data, mime);

    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO blobs (file, user_id, mime, size) VALUES (?, ?, ?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setBytes(1, filename);
      st.setInt(2, userId);
      st.setString(3, mime);
      st.setInt(4, data.length);

      st.executeUpdate();

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next()) throw new InternalErrorException(internal_blob);
        return rs.getInt(1);
      }
    } catch (SQLException exc) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void deleteBlob(int blobId) throws InternalErrorException {
    byte[] filename;

    try (PreparedStatement st =
        connection.prepareStatement("SELECT file FROM blobs WHERE id = ?")) {
      st.setInt(1, blobId);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return;
        filename = rs.getBytes(1);
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }

    fileProvider.deleteContent(filename);

    try (PreparedStatement st =
        connection.prepareStatement(
            "UPDATE blobs SET deleted=true, date_deleted=now() WHERE id = ?")) {
      st.setInt(1, blobId);
      st.executeUpdate();
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public BlobData getBlobById(int blobId) throws InternalErrorException {
    try (PreparedStatement st = connection.prepareStatement("SELECT * FROM blobs WHERE id = ?")) {
      st.setInt(1, blobId);

      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return null;
        else return getBlobData(rs);
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public byte[] getBlobData(int blobId) throws InternalErrorException {
    BlobData blobData = getBlobById(blobId);

    if (blobData == null || blobData == BlobData.DELETED) return null;
    return fileProvider.getContent(blobData.file()).data();
  }

  @Override
  public List<BlobData> getUserBlobs(int userId) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT * FROM blobs WHERE user_id = ? AND deleted=false")) {
      st.setInt(1, userId);

      try (ResultSet rs = st.executeQuery()) {
        List<BlobData> list = new ArrayList<>();
        while (rs.next()) {
          list.add(getBlobData(rs));
        }
        return list;
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  private BlobData getBlobData(ResultSet rs) throws SQLException {
    if (rs.getBoolean("deleted")) return BlobData.DELETED;
    return new BlobData(
        rs.getInt("id"),
        rs.getInt("user_id"),
        rs.getTimestamp("created").toLocalDateTime(),
        rs.getString("mime"),
        rs.getBytes("file"),
        rs.getInt("size"));
  }
}
