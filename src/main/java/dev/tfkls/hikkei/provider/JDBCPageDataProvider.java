package dev.tfkls.hikkei.provider;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.BadHandleException;
import dev.tfkls.hikkei.model.page.CommentData;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.page.RevisionData;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JDBCPageDataProvider implements PageDataProvider {
  private final Connection connection;

  public JDBCPageDataProvider(Connection conn) {
    connection = conn;
  }

  @Override
  public boolean doesPageExist(int pageId) {

    try (PreparedStatement st =
        connection.prepareStatement("SELECT COUNT(*) FROM pages WHERE id = ?")) {
      st.setInt(1, pageId);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return false;
        return rs.getInt(1) > 0;
      }
    } catch (SQLException ignored) {
    }

    return false;
  }

  @Override
  public Integer getRandomPage() {
    try (Statement st = connection.createStatement()) {
      try (ResultSet rs = st.executeQuery("SELECT id FROM pages ORDER BY RANDOM() LIMIT 1")) {
        if (!rs.next()) return null;
        return rs.getInt(1);
      }
    } catch (SQLException ignored) {
    }

    return null;
  }

  @Override
  public List<PageData> searchPageByTitle(String title) {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT * FROM pages WHERE title ILIKE ?")) {
      st.setString(1, "%" + title + "%");
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<PageData> res = new ArrayList<>();
        while (rs.next()) {
          res.add(new PageData(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getBoolean(4)));
        }
        return res;
      }
    } catch (SQLException ignored) {
    }

    return List.of();
  }

  @Override
  public PageData getPageData(int pageId) throws BadHandleException {

    try (PreparedStatement st = connection.prepareStatement("SELECT * FROM pages WHERE id = ?")) {
      st.setInt(1, pageId);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) throw new BadHandleException("Incorrect page handle");
        return new PageData(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getBoolean(4));
      }
    } catch (SQLException ignored) {
    }

    return null;
  }

  @Override
  public Optional<Integer> getPageByTitle(String title) {

    try (PreparedStatement st = connection.prepareStatement("SELECT id FROM pages WHERE url = ?")) {
      st.setString(1, title);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        else return Optional.of(rs.getInt(1));
      }
    } catch (SQLException ignored) {
    }

    return Optional.empty();
  }

  @Override
  public Integer createPage(PageData data) throws BadDataException {

    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO pages (url, category) VALUES (?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setString(1, data.title());
      st.setInt(2, data.category());
      st.executeUpdate();

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next()) throw new BadDataException("Couldn't create new page " + data.title());
        return rs.getInt(1);
      }
    } catch (SQLException ignored) {
    }
    return 0;
  }

  @Override
  public void updatePageUrl(int pageId, String url) throws BadHandleException, BadDataException {

    if (!doesPageExist(pageId)) throw new BadHandleException("Incorrect page handle");

    try (PreparedStatement st =
        connection.prepareStatement("UPDATE pages SET url = ? WHERE id = ?")) {
      st.setString(1, url);
      st.setInt(2, pageId);
      int num_rows = st.executeUpdate();
      if (num_rows < 1)
        throw new BadDataException("Couldn't update url for page " + pageId + " to " + url);
    } catch (SQLException ignored) {
    }
  }

  @Override
  public void updatePageCategory(int pageId, int categoryId) throws BadHandleException {

    if (!doesPageExist(pageId)) throw new BadHandleException("Incorrect page handle");

    try (PreparedStatement st =
        connection.prepareStatement("UPDATE pages SET category = ? WHERE id = ?")) {
      st.setInt(1, categoryId);
      st.setInt(2, pageId);
      st.executeUpdate();
    } catch (SQLException ignored) {
    }
  }

  @Override
  public void archivePage(int pageId) throws BadHandleException {
    // TODO: de-archivisation option

    if (!doesPageExist(pageId)) throw new BadHandleException("Incorrect user handle");

    try (PreparedStatement st =
        connection.prepareStatement("UPDATE pages SET archive = true WHERE id = ?")) {
      st.setInt(1, pageId);
      st.executeUpdate();
    } catch (SQLException ignored) {
    }
  }

  @Override
  public List<RevisionData> getPageRevisions(int pageId) throws BadHandleException {

    if (!doesPageExist(pageId)) throw new BadHandleException("Incorrect page handle");

    try (PreparedStatement st =
        connection.prepareStatement(
            "SELECT * FROM revisions WHERE page_id = ? ORDER BY date DESC")) {
      st.setInt(1, pageId);
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<RevisionData> res = new ArrayList<>();
        while (rs.next())
          res.add(
              new RevisionData(
                  rs.getInt(2),
                  rs.getObject(3, UUID.class),
                  rs.getTimestamp(4).toLocalDateTime(),
                  rs.getBoolean(5)));
        return res;
      }
    } catch (SQLException ignored) {
    }

    return new ArrayList<>();
  }

  @Override
  public Integer createRevision(int pageId) throws BadHandleException {

    if (!doesPageExist(pageId)) throw new BadHandleException("Incorrect page handle");

    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO revisions (page_id) VALUES (?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setInt(1, pageId);
      st.executeUpdate();

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next()) return 0;
        return rs.getInt(1);
      }
    } catch (SQLException ignored) {
    }

    return 0;
  }

  @Override
  public List<CommentData> getPageComments(int pageId) throws BadHandleException {

    if (!doesPageExist(pageId)) throw new BadHandleException("Incorrect page handle");

    try (PreparedStatement st =
        connection.prepareStatement(
            "SELECT * FROM comments WHERE page_id = ? ORDER BY date DESC")) {
      st.setInt(1, pageId);
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<CommentData> res = new ArrayList<>();
        while (rs.next())
          res.add(
              new CommentData(rs.getInt(2), rs.getString(3), rs.getTimestamp(4).toLocalDateTime()));
        return res;
      }
    } catch (SQLException ignored) {
    }

    return new ArrayList<>();
  }

  @Override
  public Integer addComment(int pageId, String content)
      throws BadHandleException, BadDataException {

    if (!doesPageExist(pageId)) throw new BadHandleException("Incorrect page handle");

    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO comments (page_id, content) VALUES (?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setInt(1, pageId);
      st.setString(2, content);
      st.executeUpdate();

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next()) return 0;
        return rs.getInt(1);
      }
    } catch (SQLException ignored) {
    }

    return 0;
  }

  @Override
  public boolean deleteComment(int commentId) throws BadHandleException {

    if (!doesPageExist(commentId)) throw new BadHandleException("Incorrect comment handle");

    try (PreparedStatement st = connection.prepareStatement("DELETE FROM comments WHERE id = ?")) {
      st.setInt(1, commentId);
      return st.executeUpdate() > 0;
    } catch (SQLException ignored) {
    }
    return false;
  }
}
