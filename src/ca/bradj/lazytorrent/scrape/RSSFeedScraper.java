package ca.bradj.lazytorrent.scrape;

import java.util.Collection;

import javafx.util.Pair;
import ca.bradj.common.base.WithConfidence;
import ca.bradj.lazytorrent.app.Logger;
import ca.bradj.lazytorrent.prefs.Preferences;
import ca.bradj.lazytorrent.rss.RSSTorrent;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class RSSFeedScraper {

	private final Preferences prefs;
	private final Logger logger;

	public RSSFeedScraper(Preferences prefs, Logger logger) {
		this.prefs = Preconditions.checkNotNull(prefs);
		this.logger = Preconditions.checkNotNull(logger);
	}

	public Collection<WithConfidence<Pair<RSSTorrent, String>>> getDownloadCandidates(Collection<RSSTorrent> torrents) {
		Preconditions.checkNotNull(torrents);

		ImmutableList<RSSTorrent> lastItems = ImmutableList.copyOf(torrents);

		Collection<WithConfidence<Pair<RSSTorrent, String>>> out = Lists
				.newArrayListWithExpectedSize(lastItems.size() / 2); // rough-heuristic
		for (String p : prefs.getList()) {
			PreferenceScrape scraper = new PreferenceScrape(p, logger);
			Collection<WithConfidence<Pair<RSSTorrent, String>>> bestMatch = scraper.chooseBestMatch(lastItems);
			out.addAll(bestMatch);
		}
		return out;
	}

}
