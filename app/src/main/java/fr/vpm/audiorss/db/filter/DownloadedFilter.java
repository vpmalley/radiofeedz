package fr.vpm.audiorss.db.filter;

import fr.vpm.audiorss.db.DbMedia;

/**
 * Created by vince on 23/11/14.
 */
public class DownloadedFilter implements SelectionFilter {

  @Override
  public String getSelectionQuery() {
    return DbMedia.T_MEDIA + "." + DbMedia.IS_DL_KEY + ">0";
  }

  @Override
  public String[] getSelectionValues() {
    return new String[0];
  }
}
