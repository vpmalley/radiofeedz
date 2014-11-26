package fr.vpm.audiorss.db.filter;

import fr.vpm.audiorss.db.DatabaseOpenHelper;

/**
 * Created by vince on 23/11/14.
 */
public class ChannelFilter implements QueryFilter.SelectionFilter {

  private final long channelId;

  @Override
  public int index() {
    return -1;
  }

  public ChannelFilter(long channelId) {
    this.channelId = channelId;
  }

  @Override
  public String getSelectionQuery() {
    return DatabaseOpenHelper.CHANNEL_ID_KEY + "=?";
  }

  @Override
  public String[] getSelectionValues() {
    return new String[]{String.valueOf(channelId)};
  }
}
