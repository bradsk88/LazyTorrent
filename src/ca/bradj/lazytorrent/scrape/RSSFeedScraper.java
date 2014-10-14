package ca.bradj.lazytorrent.scrape;

import java.util.Collection;
import java.util.Optional;

import javafx.util.Pair;
import ca.bradj.common.base.WithConfidence;
import ca.bradj.lazytorrent.app.Logger;
import ca.bradj.lazytorrent.prefs.Preferences;
import ca.bradj.lazytorrent.rss.RSSTorrent;
import ca.bradj.scrape.matching.MatchFailHandler;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class RSSFeedScraper {

	private final Preferences prefs;
	private final Logger logger;
	private Optional<MatchFailHandler<RSSTorrent>> handler = Optional.empty();

	public RSSFeedScraper(Preferences prefs, Logger logger) {
		this.prefs = Preconditions.checkNotNull(prefs);
		this.logger = Preconditions.checkNotNull(logger);
	}
	
	public RSSFeedScraper setMatchFailHandler( MatchFailHandler<RSSTorrent> handler ) {
		return setMatchFailHandler( Optional.of( handler ) );
	}
	
	public RSSFeedScraper setMatchFailHandler(
			Optional<MatchFailHandler<RSSTorrent>> handler) {
		this.handler = Preconditions.checkNotNull( handler );
		return this;
	}

	public Collection<WithConfidence<Pair<RSSTorrent, String>>> getDownloadCandidates(Collection<RSSTorrent> torrents) {
		Preconditions.checkNotNull(torrents);

		ImmutableList<RSSTorrent> lastItems = ImmutableList.copyOf(torrents);

		Collection<WithConfidence<Pair<RSSTorrent, String>>> out = Lists
				.newArrayListWithExpectedSize(lastItems.size() / 2); // rough-heuristic
		for (String p : prefs.getList()) {
			out.addAll( new PreferenceScrape(p, logger)
				.setMatchFailHandler( handler )
				.chooseBestMatch(lastItems) 
			);
		}
		return out;
	}

}
