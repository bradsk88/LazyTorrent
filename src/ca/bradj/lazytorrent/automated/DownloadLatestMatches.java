package ca.bradj.lazytorrent.automated;

import java.nio.file.Path;
import java.util.Collection;

import ca.bradj.common.base.WithConfidence;
import ca.bradj.lazytorrent.app.AlreadyDownloaded;
import ca.bradj.lazytorrent.app.DownloadScrapedItems;
import ca.bradj.lazytorrent.app.Logger;
import ca.bradj.lazytorrent.app.ScrapedItemsProvider;
import ca.bradj.lazytorrent.prefs.Preferences;
import ca.bradj.lazytorrent.rss.RSSFeed;
import ca.bradj.lazytorrent.rss.RSSTorrent;
import ca.bradj.lazytorrent.scrape.RSSFeedScraper;

import com.google.common.base.Preconditions;

public class DownloadLatestMatches implements Runnable {

	private final RSSFeed rss;
	private final Preferences prefs;
	private final AlreadyDownloaded already;
	private final Logger logger;
	private final Path rootDir;

	public DownloadLatestMatches(Path rootDir, RSSFeed rss, Preferences prefs, AlreadyDownloaded alreadyDownloaded,
			Logger logger) {
		this.rss = rss;
		this.prefs = prefs;
		this.already = alreadyDownloaded;
		this.logger = logger;
		this.rootDir = Preconditions.checkNotNull(rootDir);
	}

	@Override
	public void run() {
		try {
			Collection<RSSTorrent> rssL = rss.requestRefresh();
			Collection<WithConfidence<RSSTorrent>> scraped = new RSSFeedScraper(prefs, logger)
					.getDownloadCandidates(rssL);
			logger.debug("Downloading " + scraped.size() + " torrents if not already downloaded");
			new DownloadScrapedItems(rootDir, toSupplier(scraped), already, logger).doNow();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private ScrapedItemsProvider toSupplier(final Collection<WithConfidence<RSSTorrent>> scraped) {
		return new ScrapedItemsProvider() {

			@Override
			public Collection<RSSTorrent> getLastScrape() {
				return WithConfidence.stripConfidence(scraped);
			}
		};
	}
}
