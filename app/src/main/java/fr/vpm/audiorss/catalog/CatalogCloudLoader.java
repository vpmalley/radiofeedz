package fr.vpm.audiorss.catalog;

import android.content.Context;
import android.os.AsyncTask;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.catalog.json.CloudantDocument;
import fr.vpm.audiorss.http.DefaultNetworkChecker;

/**
 * Created by vince on 27/04/16.
 */
public class CatalogCloudLoader implements CatalogLoader {

  private final CatalogDisplay catalogDisplay;

  private final Context context;

  public CatalogCloudLoader(CatalogDisplay catalogDisplay, Context context) {
    this.catalogDisplay = catalogDisplay;
    this.context = context;
  }

  public void loadData() {
    if (new DefaultNetworkChecker().checkNetworkForRefresh(context, false)) {
      new AsyncTask<String, Integer, Catalog>() {

        @Override
        protected Catalog doInBackground(String... strings) {
          Database catalogDb = getDatabase(context);
          CloudantDocument cloudCatalog = catalogDb.find(CloudantDocument.class, context.getString(R.string.rf_catalog_id));
          Catalog catalog = new Catalog(cloudCatalog.getCatalog());
          return catalog;
        }

        @Override
        protected void onPostExecute(Catalog catalog) {
          super.onPostExecute(catalog);
          catalogDisplay.displayCatalog(catalog);
        }
      }.execute("");
    }

  }

  private Database getDatabase(Context context) {
    CloudantClient client = ClientBuilder.account("radiofeedz")
        .username(context.getString(R.string.rf_catalog_username))
        .password(context.getString(R.string.rf_catalog_pwd))
        .build();
    return client.database("rf_catalog", false);
  }
}
