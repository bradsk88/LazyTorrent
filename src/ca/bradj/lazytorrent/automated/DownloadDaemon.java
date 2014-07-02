package ca.bradj.lazytorrent.automated;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ca.bradj.lazytorrent.app.AppConfig;
import ca.bradj.lazytorrent.app.Logger;
import ca.bradj.lazytorrent.rss.RSSFeed;

public class DownloadDaemon {

	public static ScheduledExecutorService start(RSSFeed rss, Logger logger,
			AppConfig appConfig) {
		ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);
		ex.scheduleAtFixedRate(
				new DownloadLatestMatches(rss, logger, appConfig), 5, 900,
				TimeUnit.SECONDS);
		return ex;
	}

}
