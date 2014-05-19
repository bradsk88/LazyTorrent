package ca.bradj.lazytorrent.automated;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ca.bradj.lazytorrent.app.AlreadyDownloaded;
import ca.bradj.lazytorrent.app.Logger;
import ca.bradj.lazytorrent.prefs.Preferences;
import ca.bradj.lazytorrent.rss.RSSFeed;

public class DownloadDaemon {

	public static ScheduledExecutorService start(Path rootDir, RSSFeed rss, Preferences prefs,
			AlreadyDownloaded alreadyDownloaded, Logger logger) {
		ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);
		ex.scheduleAtFixedRate(new DownloadLatestMatches(rootDir, rss, prefs, alreadyDownloaded, logger), 5, 900,
				TimeUnit.SECONDS);
		return ex;
	}

}
