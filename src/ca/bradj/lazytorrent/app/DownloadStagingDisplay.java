package ca.bradj.lazytorrent.app;

import java.nio.file.Path;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import ca.bradj.Layouts;
import ca.bradj.lazytorrent.prefs.Preferences;
import ca.bradj.lazytorrent.rss.RSSListView;
import ca.bradj.lazytorrent.scrape.RSSFeedScraper;
import ca.bradj.lazytorrent.scrape.ScrapeListView;
import ca.bradj.lazytorrent.scrape.ShowScrapedItems;

import com.google.common.base.Preconditions;

public class DownloadStagingDisplay {

	private static final String SCRAPE = "Select download candidates";
	private static final String DOWNLOAD = "Download";
	private final Node node;
	private final Path rootDir;

	public DownloadStagingDisplay(Path rootDir, RSSListView rssListView, Preferences prefs,
			AlreadyDownloaded alreadyDownloaded, Logger logger) {
		this.rootDir = Preconditions.checkNotNull(rootDir);
		RSSFeedScraper scraper = new RSSFeedScraper(prefs, logger);
		this.node = makeNode(scraper, alreadyDownloaded, rssListView, logger);
	}

	private Node makeNode(RSSFeedScraper scraper, AlreadyDownloaded alreadyDownloaded, RSSListView rssListView,
			Logger logger) {
		ScrapeListView listView = new ScrapeListView();
		Button button = new Button(SCRAPE);
		button.setOnAction(new ShowScrapedItems(rssListView, listView, scraper));

		Button button2 = new Button(DOWNLOAD);
		button2.setOnAction(new DownloadScrapedItems(rootDir, listView, alreadyDownloaded, logger));

		BorderPane buttons = BorderPaneBuilder.create().left(button).right(button2).build();

		return Layouts.vBoxBuild().children(listView.getNode(), buttons).build();
	}

	public Node getNode() {
		return node;
	}

}
