package ca.bradj.lazytorrent.app;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
		for (RSSTorrent i : listView.getLastScrape()) {
			if (alreadyDownloaded.isSameNameAndEpisode(i)) {
				continue;
			}

			try {
				Failable<File> f = Torrent.download(currentTorrentsDir, i);
				if (f.isSuccess()) {
					Torrent.openAndStart(f.get(), logger);
					alreadyDownloaded.add(i);
					logger.log("Succesfully opened: " + f.get().getName());
					continue;
				}
				logger.log("Failed to download because: " + f.getReason());
				System.err.println(f.getReason());
			} catch (MalformedURLException e) {
				alreadyDownloaded.add(i);
				logger.error(e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				alreadyDownloaded.add(i);
				logger.error(e.getMessage());
				e.printStackTrace();
			}

		}
	}

}
