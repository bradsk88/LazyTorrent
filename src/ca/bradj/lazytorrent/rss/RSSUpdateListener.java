package ca.bradj.lazytorrent.rss;

import com.google.common.collect.ImmutableList;

public interface RSSUpdateListener {

	void newTorrentsListAvailable(ImmutableList<RSSTorrent> torrents);
}
