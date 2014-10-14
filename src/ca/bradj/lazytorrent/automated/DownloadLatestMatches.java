package ca.bradj.lazytorrent.automated;

import java.util.Collection;

import javafx.util.Pair;
import ca.bradj.common.base.WithConfidence;
import ca.bradj.lazytorrent.app.AppConfig;
import ca.bradj.lazytorrent.app.DownloadScrapedItems;
import ca.bradj.lazytorrent.app.Logger;
import ca.bradj.lazytorrent.app.ScrapedItemsProvider;
import ca.bradj.lazytorrent.prefs.Preferences;
import ca.bradj.lazytorrent.rss.RSSFeed;
import ca.bradj.lazytorrent.rss.RSSTorrent;
import ca.bradj.lazytorrent.scrape.RSSFeedScraper;
import ca.bradj.scrape.matching.MatchFailHandler;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class DownloadLatestMatches implements Runnable {

	private final RSSFeed rss; 
	private final Logger logger;
	private AppConfig appConfig;

	public DownloadLatestMatches(RSSFeed rss, Logger logger, AppConfig appCfg) {
		this.rss = rss;
		this.logger = logger;
		this.appConfig = Preconditions.checkNotNull(appCfg);
	}
	
	@Override
	public void run() {
		try {
			Collection<RSSTorrent> rssL = rss.requestRefresh();
			Collection<WithConfidence<Pair<RSSTorrent, String>>> scraped = new RSSFeedScraper( appConfig.getPrefs(), logger)
				.setMatchFailHandler( appConfig.getMatchFailHandler() )
				.getDownloadCandidates(rssL);
			logger.debug("Downloading " + scraped.size()
					+ " torrents if not already downloaded");
			new DownloadScrapedItems(appConfig, toSupplier(scraped), logger)
					.doNow();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private ScrapedItemsProvider toSupplier(
			final Collection<WithConfidence<Pair<RSSTorrent, String>>> scraped) {
		return new ScrapedItemsProvider() {

			@Override
			public Collection<Pair<RSSTorrent, String>> getLastScrape() {
				return WithConfidence.stripConfidence(scraped);
			}
		};
	}
	
}
