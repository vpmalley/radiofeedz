package fr.vpm.audiorss.exception;

import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 26/05/16.
 */
public class RetrieveException extends Exception {

  private RSSChannel failingRSSChannel;

  public RetrieveException(RSSChannel failingRSSChannel) {
    this.failingRSSChannel = failingRSSChannel;
  }

  public RSSChannel getFailingRSSChannel() {
    return failingRSSChannel;
  }
}
