package fr.vpm.audiorss;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

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
    if (Intent.ACTION_SEND.equals(i.getAction()) && ("text/plain".equals(i.getType()))) {
      intent.putExtra(CHANNEL_NEW_URL, i.getStringExtra(Intent.EXTRA_TEXT));
    } else {
      intent.putExtra(CHANNEL_NEW_URL, i.getData().toString());
    }

    TextView emptyView = new TextView(this);
    emptyView.setText(R.string.press_back);
    setContentView(emptyView);

    startActivity(intent);
  }
}
