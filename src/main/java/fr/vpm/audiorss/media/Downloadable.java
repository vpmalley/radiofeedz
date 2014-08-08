public interface Downloadable {

  public void download(final Context context);

  public long getDownloadId();

  public boolean isDownloaded();

  public String getDistantUrl();

}