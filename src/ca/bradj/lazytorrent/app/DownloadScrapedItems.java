package ca.bradj.lazytorrent.app;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
		for (Pair<RSSTorrent, String> i : listView.getLastScrape()) {
			RSSTorrent torrent = i.getKey();
			if (alreadyDownloaded.isSameNameAndEpisode(torrent)) {
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
				logger.log("Failed to download because: " + f.getReason());
				System.err.println(f.getReason());
			} catch (MalformedURLException e) {
				alreadyDownloaded.add(torrent);
				logger.error(e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				alreadyDownloaded.add(torrent);
				logger.error(e.getMessage());
				e.printStackTrace();
			}

		}
	}

}
