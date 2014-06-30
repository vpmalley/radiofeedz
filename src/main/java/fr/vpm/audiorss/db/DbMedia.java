package fr.vpm.audiorss.db;

import android.content.ContentValues;
import fr.vpm.audiorss.media.Media;

public class DbMedia {
	
	private final String NAME_KEY = "name";

	private final String TITLE_KEY = "title";

	private final String INET_URL_KEY = "inet_url";

	private final String DEVICE_URI_KEY = "device_uri";

	private final String DL_ID_KEY = "download_id";

	public ContentValues createContentValues(Media media) {
		ContentValues channelValues = new ContentValues();
		channelValues.put(NAME_KEY, media.getName());
		channelValues.put(TITLE_KEY, media.getNotificationTitle());
		channelValues.put(INET_URL_KEY, media.getInetUrl());
		channelValues.put(DEVICE_URI_KEY, media.getDeviceUri());
		channelValues.put(DL_ID_KEY, media.getDownloadId());
		return channelValues;
	}

	
}
