package fr.vpm.audiorss;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import fr.vpm.audiorss.http.AsyncFeedSearch;
import fr.vpm.audiorss.http.SearchResult;

public class SearchFeedActivity extends Activity {

	EditText searchQuery;

	ListView mResults;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// a bar for search and a list of results
		setContentView(R.layout.activity_searchfeed);

		searchQuery = (EditText) findViewById(R.id.search_query);

		ImageButton searchButton = (ImageButton) findViewById(R.id.launch_search);
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchFeedSearch();
			}
		});

	}

	public void refreshView() {

		ArrayAdapter<SearchResult> searchAdapter = new ArrayAdapter<SearchResult>(
				this, R.layout.activity_item, SearchResult.lastSearch);
		// fill ListView with all the items
		ListView results = (ListView) findViewById(R.id.search_results);
		results.setAdapter(searchAdapter);
	}

	private void launchFeedSearch() {
		new AsyncFeedSearch(this).execute(searchQuery.getText().toString());
	}

}
