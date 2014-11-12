package ca.bradj.lazytorrent.app;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;

import org.slf4j.Logger;

import ca.bradj.Layouts;
import ca.bradj.lazytorrent.rss.RSSListView;
import ca.bradj.lazytorrent.scrape.RSSFeedScraper;
import ca.bradj.lazytorrent.scrape.ScrapeListView;
import ca.bradj.lazytorrent.scrape.ShowScrapedItems;

public class DownloadStagingDisplay {

	private static final String SCRAPE = "Select download candidates";
	private static final String DOWNLOAD = "Download";
	private final Node node;

	public DownloadStagingDisplay(RSSListView rssListView, AppConfig appConfig,
			Logger logger) {
		RSSFeedScraper scraper = new RSSFeedScraper(appConfig.getPrefs(),
				logger);
		this.node = makeNode(scraper, rssListView, logger, appConfig);
	}

	private Node makeNode(RSSFeedScraper scraper, RSSListView rssListView,
			Logger logger, AppConfig appConfig) {
		ScrapeListView listView = new ScrapeListView();
		Button button = new Button(SCRAPE);
		button.setOnAction(new ShowScrapedItems(rssListView, listView, scraper));

		Button button2 = new Button(DOWNLOAD);
		button2.setOnAction(new DownloadScrapedItems(appConfig, listView,
				logger));

		BorderPane buttons = BorderPaneBuilder.create().left(button)
				.right(button2).build();

		return Layouts.vBoxBuild().children(listView.getNode(), buttons)
				.build();
	}

	public Node getNode() {
		return node;
	}

}
