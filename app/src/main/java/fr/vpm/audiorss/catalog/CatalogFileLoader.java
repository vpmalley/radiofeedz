package fr.vpm.audiorss.catalog;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.catalog.json.FeedGroup;

/**
 * Created by vince on 27/04/16.
 */
public class CatalogFileLoader implements CatalogLoader {

  private final CatalogDisplay catalogDisplay;

  private final Context context;

  public CatalogFileLoader(CatalogDisplay catalogDisplay, Context context) {
    this.catalogDisplay = catalogDisplay;
    this.context = context;
  }

  public void loadData() {
    List<FeedGroup> feeds = new ArrayList<>();
    InputStream catalogStream = null;
    try {
      catalogStream = context.getResources().openRawResource(R.raw.catalog);
      Reader catalogReader = new InputStreamReader(catalogStream);

      Type feedCollection = new TypeToken<List<FeedGroup>>(){}.getType();
      feeds = new Gson().fromJson(catalogReader, feedCollection);
    } finally {
      if (catalogStream != null){
        try {
          catalogStream.close();
        } catch (IOException e) {
          Log.e("file", e.getMessage());
        }
      }
    }
    Catalog catalog = new Catalog(feeds);
    catalogDisplay.displayCatalog(catalog);
  }
}
