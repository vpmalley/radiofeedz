package fr.vpm.audiorss.http;

import java.io.IOException;
import java.text.ParseException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParserException;

import android.os.AsyncTask;
import android.util.Log;
import fr.vpm.audiorss.FeedsActivity;
import fr.vpm.audiorss.process.ItemParser;
import fr.vpm.audiorss.rss.RSSChannel;

public class AsyncFeedRefresh extends AsyncTask<String, Integer, RSSChannel> {

	String url;

	FeedsActivity activity;

	Exception mE = null;

	public AsyncFeedRefresh(FeedsActivity activity) {
		this.activity = activity;
	}

	public HttpEntity refresh(String rssUrl) throws ClientProtocolException,
			IOException {
		HttpEntity entity = null;
		HttpUriRequest req = new HttpGet(rssUrl);
		Log.d("refresh", "sending a request for " + rssUrl);

		HttpResponse resp = new DefaultHttpClient().execute(req);
		if (HttpStatus.SC_OK == resp.getStatusLine().getStatusCode()) {
			entity = resp.getEntity();
			Log.d("refresh", "received resp " + resp.getStatusLine().toString());
		} else {
			// build an error message
			throw new IOException("Connection problem : "
					+ resp.getStatusLine().toString());
		}

		return entity;
	}

	@Override
	protected RSSChannel doInBackground(String... params) {
		boolean hasError = false;
		url = params[0];
		HttpEntity entity = null;
		RSSChannel newChannel = null;
		try {
			entity = refresh(url);
		} catch (ClientProtocolException e) {
			hasError = true;
			mE = e;
		} catch (IOException e) {
			hasError = true;
			mE = e;
		}

		try {
			if (!hasError) {
				Log.d("refresh", "received a response for feed");
				newChannel = new ItemParser().parseChannel(entity, url);
				Log.d("refresh", "created feed from " + newChannel.getUrl());
			}
		} catch (XmlPullParserException e) {
			mE = e;
		} catch (IOException e) {
			mE = e;
		} catch (ParseException e) {
			mE = e;
		}
		return newChannel;
	}

	@Override
	protected void onPostExecute(RSSChannel newChannel) {
		if (mE != null) {
			handleException(mE);
		} else {
			try {
				newChannel.saveToDb(activity);
			} catch (ParseException e) {
				handleException(e);
			}
			activity.refreshView();
		}
		super.onPostExecute(newChannel);
	}

	private void handleException(Exception e) {
		activity.displayError(e.getLocalizedMessage());
		Log.e("Exception", e.toString());
	}
}
