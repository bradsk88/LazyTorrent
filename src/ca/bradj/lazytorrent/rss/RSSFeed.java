package ca.bradj.lazytorrent.rss;

import java.util.Collection;

public interface RSSFeed {

	Collection<RSSTorrent> requestRefresh();

	void addUpdateListener(RSSUpdateListener listener);

}
