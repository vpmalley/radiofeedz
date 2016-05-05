package fr.vpm.audiorss;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import fr.vpm.audiorss.catalog.Catalog;
import fr.vpm.audiorss.catalog.CatalogCloudLoader;
import fr.vpm.audiorss.catalog.CatalogDisplay;
import fr.vpm.audiorss.catalog.CatalogFileLoader;
import fr.vpm.audiorss.catalog.CatalogLoader;
import fr.vpm.audiorss.http.DefaultNetworkChecker;


public class CatalogActivity extends Activity implements CatalogDisplay {

  public static final String FEED_URL_EXTRA = "feed_url";
  private ExpandableListView feedsView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_catalog);

    feedsView = (ExpandableListView) findViewById(R.id.catalog);

    getCatalogLoader().loadData();
  }

  @NonNull
  private CatalogLoader getCatalogLoader() {
    CatalogLoader catalogLoader;
    if (new DefaultNetworkChecker().checkNetworkForDownload(this, false)) {
      catalogLoader = new CatalogCloudLoader(this, this);
    } else {
      catalogLoader = new CatalogFileLoader(this, this);
    }
    return catalogLoader;
  }

  @Override
  public void displayCatalog(final Catalog catalog) {
    ExpandableListAdapter feedsAdapter = new SimpleExpandableListAdapter(this, catalog.getGroups(), Catalog.GROUP_LAYOUT,
            Catalog.GROUP_KEYS, Catalog.GROUP_VIEWS, catalog.getChildren(), Catalog.CHILD_LAYOUT, Catalog.CHILD_KEYS, Catalog.CHILD_VIEWS);
    feedsView.setAdapter(feedsAdapter);


    feedsView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
      @Override
      public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Intent i = new Intent();
        i.putExtra(FEED_URL_EXTRA, catalog.getUrl(groupPosition, childPosition));
        setResult(RESULT_OK, i);
        finish();
        return true;
      }
    });
  }
}
