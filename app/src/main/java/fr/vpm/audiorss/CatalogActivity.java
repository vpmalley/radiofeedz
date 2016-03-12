package fr.vpm.audiorss;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import fr.vpm.audiorss.catalog.Catalog;


public class CatalogActivity extends Activity {

  public static final String FEED_URL_EXTRA = "feed_url";
  private Catalog feedsCatalog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_catalog);

    ExpandableListView feedsView = (ExpandableListView) findViewById(R.id.catalog);
    feedsView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
      @Override
      public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Intent i = new Intent();
        i.putExtra(FEED_URL_EXTRA, feedsCatalog.getUrl(groupPosition, childPosition));
        setResult(RESULT_OK, i);
        finish();
        return true;
      }
    });

    feedsCatalog = new Catalog();
    feedsCatalog.loadData(this);
    ExpandableListAdapter feedsAdapter = new SimpleExpandableListAdapter(this, feedsCatalog.getGroups(), Catalog.GROUP_LAYOUT,
            Catalog.GROUP_KEYS, Catalog.GROUP_VIEWS, feedsCatalog.getChildren(), Catalog.CHILD_LAYOUT, Catalog.CHILD_KEYS, Catalog.CHILD_VIEWS);
    feedsView.setAdapter(feedsAdapter);
  }
}
