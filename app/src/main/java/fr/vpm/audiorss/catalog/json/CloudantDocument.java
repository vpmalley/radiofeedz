package fr.vpm.audiorss.catalog.json;

import java.util.List;

/**
 * Created by vince on 05/05/16.
 */
public class CloudantDocument {

  String _id;

  String rev;

  List<FeedGroup> catalog;

  public List<FeedGroup> getCatalog() {
    return catalog;
  }
}
