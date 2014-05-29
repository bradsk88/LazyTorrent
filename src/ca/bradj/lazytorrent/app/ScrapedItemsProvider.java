package ca.bradj.lazytorrent.app;

import java.util.Collection;

import javafx.util.Pair;
import ca.bradj.lazytorrent.rss.RSSTorrent;

public interface ScrapedItemsProvider {

	Collection<Pair<RSSTorrent, String>> getLastScrape();

}
