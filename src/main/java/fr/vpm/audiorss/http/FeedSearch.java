package fr.vpm.audiorss.http;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.JsonReader;

public class FeedSearch {

  private static final char SEP = '&';

  static final String BASE_URL = "https://www.googleapis.com/customsearch/v1?";

  static final String P_QUERY = "q=";

  static final String P_FILETYPE = "fileType=";

  static final String P_KEY = "key=";

  static final String SEARCH_KEY = "AIzaSyCjqq6hmvOybV5uV1IHbkjzqfZATSNDxhU";

  /**
   * 
   * @param query
   * @return possible urls of the feed
   * @throws IOException
   * @throws ClientProtocolException
   */
  public List<SearchResult> query(String query) throws ClientProtocolException,
      IOException {

    HttpUriRequest req = buildRequest(query);
    HttpResponse resp = new DefaultHttpClient().execute(req);
    List<SearchResult> array = null;
    if (HttpStatus.SC_OK == resp.getStatusLine().getStatusCode()) {
      HttpEntity entity = resp.getEntity();
      JsonReader reader = new JsonReader(new InputStreamReader(
          entity.getContent()));
      try {
        reader.beginObject();
        while ((reader.hasNext()) && (!reader.nextName().equals("items"))) {
        }
        array = readResults(reader);
        reader.endObject();
      } finally {
        reader.close();
      }
    } else {
      // build an error message
      throw new IOException("Connection problem : "
          + resp.getStatusLine().toString() + " for " + req.getURI().toString());
    }

    return array;

  }

  private List<SearchResult> readResults(JsonReader reader) throws IOException {
    List<SearchResult> results = new ArrayList<SearchResult>();
    reader.beginArray();
    while (reader.hasNext()) {
      SearchResult result = readResult(reader);
      results.add(result);
    }
    reader.endArray();
    return results;
  }

  private SearchResult readResult(JsonReader reader) throws IOException {
    String displayLink = "";
    String htmlTitle = "";
    String htmlFormattedUrl = "";
    String kind = "";
    String link = "";
    String snippet = "";
    String title = "";
    String formattedUrl = "";

    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();

      if (SearchResult.DISPLINK_TAG.equals(name)) {
        displayLink = reader.nextString();
      } else if (SearchResult.HTML_TITLE_TAG.equals(name)) {
        htmlTitle = reader.nextString();
      } else if (SearchResult.HTML_URL_TAG.equals(name)) {
        htmlFormattedUrl = reader.nextString();
      } else if (SearchResult.KIND_TAG.equals(name)) {
        kind = reader.nextString();
      } else if (SearchResult.LINK_TAG.equals(name)) {
        link = reader.nextString();
      } else if (SearchResult.SNIPPET_TAG.equals(name)) {
        snippet = reader.nextString();
      } else if (SearchResult.TITLE_TAG.equals(name)) {
        title = reader.nextString();
      } else if (SearchResult.URL_TAG.equals(name)) {
        formattedUrl = reader.nextString();
      }
    }
    reader.endObject();
    SearchResult result = new SearchResult(kind, title, htmlTitle, link,
        displayLink, snippet, formattedUrl, htmlFormattedUrl);
    return result;
  }

  private HttpUriRequest buildRequest(String query) {
    String url = buildUrl(query);
    return new HttpGet(url);
  }

  private String buildUrl(String query) {
    StringWriter writer = new StringWriter();
    writer.append(BASE_URL);
    writer.append(P_QUERY);
    writer.append("rss+");
    writer.append(query.replace(' ', '+'));
    writer.append(SEP);
    writer.append(P_FILETYPE);
    writer.append("xml");
    writer.append(SEP);
    writer.append(P_KEY);
    writer.append(SEARCH_KEY);
    return writer.toString();
  }
}
