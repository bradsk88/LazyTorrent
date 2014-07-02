package ca.bradj.lazytorrent.app;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import com.google.common.base.Preconditions;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Pair;
import ca.bradj.common.base.Failable;
import ca.bradj.common.base.Preconditions2;
import ca.bradj.lazytorrent.connection.Torrent;
import ca.bradj.lazytorrent.rss.RSSTorrent;
import ca.bradj.lazytorrent.scrape.ScrapeListView;
import com.google.common.base.Preconditions;

public class DownloadScrapedItems implements EventHandler<ActionEvent> {

	private final ScrapedItemsProvider listView;
	private final Logger logger;
	private final Path currentTorrentsDir;
	private final String torrentCommand;
	private final AlreadyDownloaded alreadyDownloaded;

	public DownloadScrapedItems(AppConfig appConfig, ScrapedItemsProvider items,
			Logger logger2) {
		this.currentTorrentsDir = Paths.get(appConfig.getRoot()+ File.separator + Torrent.TORRENTS_FOLDERNAME);
		this.listView = Preconditions.checkNotNull(items);
		this.logger = Preconditions.checkNotNull(logger2);
		this.alreadyDownloaded = appConfig.getAlreadyDownloaded();
		this.torrentCommand = appConfig.getTorrentCommand();
	}

	@Override
	public void handle(ActionEvent arg0) {
		doNow();
	}

	public void doNow() {
		Collection<Pair<RSSTorrent, String>> lastScrape = listView.getLastScrape();
		for (Pair<RSSTorrent, String> i : lastScrape) {
			RSSTorrent torrent = i.getKey();
			if (!alreadyDownloaded.shouldDownload(torrent)) {
				continue;
			}

			try {
				Failable<File> f = Torrent.download(currentTorrentsDir, torrent);
				if (f.isSuccess()) {
					Torrent.openAndStart(f.get(), logger, torrentCommand);
					alreadyDownloaded.add(torrent);
					logger.log("Succesfully opened: \"" + f.get().getName() + "\" because it matched: \""
							+ i.getValue() + "\"");
					continue;
				}
				logger.log("Failed to download " + torrent.getName().substring(0, 40) + " because: " + f.getReason());
				System.err.println(f.getReason());
			} catch (MalformedURLException e) {
				alreadyDownloaded.add(torrent);
				logger.error(e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}

		}
	}

}
