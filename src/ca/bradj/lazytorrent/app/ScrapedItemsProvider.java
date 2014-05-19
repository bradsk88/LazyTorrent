package ca.bradj.lazytorrent.app;

import java.util.Collection;

import ca.bradj.lazytorrent.rss.RSSTorrent;

public interface ScrapedItemsProvider {

	Collection<RSSTorrent> getLastScrape();

}
