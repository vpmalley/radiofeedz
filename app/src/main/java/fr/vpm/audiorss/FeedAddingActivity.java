package fr.vpm.audiorss;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by vince on 17/11/14.
 */
public class FeedAddingActivity extends Activity {

  public static final String CHANNEL_NEW_URL = "channelNewUrl";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent i = getIntent();
    Intent intent = new Intent(this, AllFeedItems.class);
    intent.putExtra(CHANNEL_NEW_URL, i.getData().toString());
    startActivity(intent);
  }
}
