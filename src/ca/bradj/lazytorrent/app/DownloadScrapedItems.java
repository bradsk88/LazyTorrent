package ca.bradj.lazytorrent.app;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Pair;
import ca.bradj.common.base.Failable;
import ca.bradj.lazytorrent.connection.Torrent;
import ca.bradj.lazytorrent.rss.RSSTorrent;

public class DownloadScrapedItems implements EventHandler<ActionEvent> {

	private final ScrapedItemsProvider listView;
	private final AlreadyDownloaded alreadyDownloaded;
	private final Logger logger;
	private final Path currentTorrentsDir;

	public DownloadScrapedItems(Path root, ScrapedItemsProvider listView, AlreadyDownloaded alreadyDownloaded,
			Logger logger) {
		this.listView = listView;
		this.alreadyDownloaded = alreadyDownloaded;
		this.logger = logger;
		this.currentTorrentsDir = Paths.get(root + File.separator + Torrent.TORRENTS_FOLDERNAME);
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
					Torrent.openAndStart(f.get(), logger);
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
