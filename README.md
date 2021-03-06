radiofeedz
==========

Your favourite *podcasts*, always with you in your pocket.

With RadioFeedz you can get, save and listen to your favourite podcast, right in your pocket.

<a href="https://play.google.com/store/apps/details?id=fr.vpm.audiorss">
  <img alt="Android app on Google Play"
       src="https://developer.android.com/images/brand/en_app_rgb_wo_60.png" />
</a>

[Download it now](/radiofeedz.apk)

## How does it work ?

You add your favourite RSS feed (be it a podcast feed, a news feed, a picture feed) to RadioFeedz.

RadioFeedz gives you the latest news and podcasts any time while online.

You download your podcasts to your Android device with few clicks.

All your podcasts/news are in your pocket to listen, read, watch anytime, on the bus, on the plane or in your living room.


## How do I find these "RSS feeds" ?

### Media

Most (if not all) news websites have RSS feeds. There is usually a link on the home page to the RSS feeds (search for RSS, for this icon <img alt="RSS feed icon, from Wikimedia, uploaded by user Anomie" src="https://upload.wikimedia.org/wikipedia/en/thumb/4/43/Feed-icon.svg/128px-Feed-icon.svg.png" height="22px" width="22px" />, for podcasts, ...).

Once you find one of these feeds, copy the url of the RSS feed, then open your RadioFeedz app and press the <img alt="'new' icon from the Icon pack provided by Android" src="/screenshots/ic_action_new.png" height="18px" width="18px" /> icon at the top-right of the main screen. It is going to suggest you to add this feed. Press Yes and enjoy your podcasts and news!

<img alt="action bar where you can see the '+' button" src="/screenshots/actionbar.png" width="400px" />

### Search engines

There are some search engines or RSS feed providers available on the web. You can try the following:

- [Instant RSS search](http://ctrlq.org/rss/) lets you search for existing media with RSS feeds
- [Feedzilla](http://www.feedzilla.com/gallery) offers a gallery of RSS feeds searchable by category

Once you picked a RSS feed among them, long-click on it if you are on your Android browser. Click on Share and you should be able to choose Radiofeedz. It will redirect you to the app, and you will be able to read the news from this source.

### Examples

You can add feeds for most radios around the world. Let's take the example of the BBC World Service. You can go to the page for the podcast for a talk-show like [`the World this week`][twtw] and long-click on the [link to the rss feed][twtw-rss]. You will see the option to open an application, click it and you will see the Radiofeedz option. Choose it and it will suggest to add this feed to your list.

<img alt="multiple applications to open the feed url" src="/screenshots/intent-fr.png" width="400px" align="center" />


It is possible to listen to the recently popular podcast [*Serial*][serial] by going to the `subscribe` page (there is the small icon). There you copy the url of the link [`RSS Feed`][serial-rss]. You open your RadioFeedz app and click the <img alt="'new' icon from the Icon pack provided by Android" src="/screenshots/ic_action_new.png" height="18px" width="18px" /> button at the top-right of the screen. Hop, you're good to listen this investigation.

## I love this app

Great! Let your friends know, let them enjoy their podcasts as well.

## There are bugs, the application crashes, what do I do ?

I tried to test my app as much as I could, but I probably forgot some bugs. Do not hesitate to [open an issue in Github][gh-issues] if you find one. If you do, please be as precise as you can (what feed you used, what button you pressed ...). In particular, some feeds might not work as expected (as I tested only the few feeds I follow).

## References

[RSS feed icon from Wikimedia][rss-wiki], by [user Anomie][anomie-wiki]
<br>
[Iconography and icon pack for Android][android-icons]

This application uses some third-party libraries that help build quality applications :
- [Apache Commons Lang (Lang3.0)][lang3] and [Apache Commons IO][commons-io]
- [Gson] for JSON deserialization
- [Glide] for picture loading
- [java-cloudant][cloudant] for server synchronisation (in particular Analytics to figure how to improve the app)
- [Acra] for bug tracking

[gh-issues]: https://github.com/vpmalley/radiofeedz/issues
[rss-icon-wiki]: https://upload.wikimedia.org/wikipedia/en/thumb/4/43/Feed-icon.svg/128px-Feed-icon.svg.png
[rss-wiki]: https://en.wikipedia.org/wiki/File:Feed-icon.svg
[anomie-wiki]: https://en.wikipedia.org/wiki/User:Anomie
[fi]: http://www.franceinter.fr
[serial]: http://serialpodcast.org/
[serial-rss]: http://feeds.serialpodcast.org/serialpodcast
[twtw]: http://www.bbc.co.uk/podcasts/series/twtw
[twtw-rss]: http://downloads.bbc.co.uk/podcasts/worldservice/twtw/rss.xml
[android-icons]: https://developer.android.com/design/style/iconography.html
[lang3]: https://commons.apache.org/proper/commons-lang/
[commons-io]: https://commons.apache.org/proper/commons-io/
[Gson]: https://github.com/google/gson
[Glide]: https://github.com/bumptech/glide
[cloudant]: https://github.com/cloudant/java-cloudant
[Acra]: https://github.com/ACRA/acra
