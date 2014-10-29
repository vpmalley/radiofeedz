package fr.vpm.audiorss.http;

import java.util.List;

public class SearchResult {

  public static final String KIND_TAG = "kind";
  public static final String TITLE_TAG = "title";
  public static final String HTML_TITLE_TAG = "htmlTitle";
  public static final String LINK_TAG = "link";
  public static final String DISPLINK_TAG = "displayLink";
  public static final String SNIPPET_TAG = "snippet";
  public static final String URL_TAG = "formattedUrl";
  public static final String HTML_URL_TAG = "htmlFormattedUrl";

  String kind;

  String title;

  String htmlTitle;

  // the useful
  String link;

  String displayLink;

  String snippet;

  String formattedUrl;

  String htmlFormattedUrl;

  public static List<SearchResult> lastSearch;

  public SearchResult(String kind, String title, String htmlTitle, String link, String displayLink,
                      String snippet, String formattedUrl, String htmlFormattedUrl) {
    super();
    this.kind = kind;
    this.title = title;
    this.htmlTitle = htmlTitle;
    this.link = link;
    this.displayLink = displayLink;
    this.snippet = snippet;
    this.formattedUrl = formattedUrl;
    this.htmlFormattedUrl = htmlFormattedUrl;
  }

}
