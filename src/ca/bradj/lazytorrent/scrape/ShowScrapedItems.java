package ca.bradj.lazytorrent.scrape;

import java.util.Collection;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Pair;
import ca.bradj.common.base.WithConfidence;
import ca.bradj.lazytorrent.rss.RSSListView;
import ca.bradj.lazytorrent.rss.RSSTorrent;

public class ShowScrapedItems implements EventHandler<ActionEvent> {

	private final RSSFeedScraper scraper;
	private final ScrapeListView listView;
	private final RSSListView rssList;

	public ShowScrapedItems(RSSListView rssList, ScrapeListView listView, RSSFeedScraper scraper) {
		this.listView = listView;
		this.scraper = scraper;
		this.rssList = rssList;
	}

	@Override
	public void handle(ActionEvent arg0) {
		Collection<WithConfidence<Pair<RSSTorrent, String>>> candidates = scraper.getDownloadCandidates(rssList
				.getCurrentItems());
		listView.setItems(candidates);
	}

}
