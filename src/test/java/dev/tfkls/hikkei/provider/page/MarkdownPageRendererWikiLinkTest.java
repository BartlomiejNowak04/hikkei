package dev.tfkls.hikkei.provider.page;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MarkdownPageRendererWikiLinkTest {
  private final MarkdownPageRenderer renderer = new MarkdownPageRenderer();

  @Test
  void singleWordWikiLinkPointsAtTheTitle() {
    String html = renderer.render("See [[Kiwi]].");
    assertTrue(html.contains("<a href=\"Kiwi\">Kiwi</a>"), html);
  }

  @Test
  void spacesInWikiLinkBecomeUnderscoresLikeTheFrontendUrl() {
    // frontend titleToUrl("Other Page") == "Other_Page", and GET /api/page/Other_Page maps back.
    String html = renderer.render("See [[Other Page]].");
    assertTrue(html.contains("<a href=\"Other_Page\">Other Page</a>"), html);
  }

  @Test
  void pipedWikiLinkKeepsCustomTextAndSlugsTheTarget() {
    // flexmark's pipe order is [[text|target]] (LINK_FIRST_SYNTAX is off by default).
    String html = renderer.render("See [[this one|Other Page]].");
    assertTrue(html.contains("<a href=\"Other_Page\">this one</a>"), html);
  }

  @Test
  void otherCharactersAreLeftAlone() {
    String html = renderer.render("See [[C++ FAQ]].");
    assertTrue(html.contains("<a href=\"C++_FAQ\">C++ FAQ</a>"), html);
  }
}
