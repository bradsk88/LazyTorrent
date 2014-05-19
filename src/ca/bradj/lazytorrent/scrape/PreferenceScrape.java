package ca.bradj.lazytorrent.scrape;

import java.util.Collection;
import java.util.Collections;

import ca.bradj.common.base.Confidence;
import ca.bradj.common.base.Failable;
import ca.bradj.common.base.WithConfidence;
import ca.bradj.lazytorrent.app.Logger;
import ca.bradj.lazytorrent.matching.Matching;
import ca.bradj.lazytorrent.rss.RSSTorrent;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class PreferenceScrape {

	private static final String P480 = "480p";
	private static final String P720 = "720p";
	private static final String P1080 = "1080p";
	private static final Failable<WithConfidence<RSSTorrent>> NO_MATCHES = Failable.fail("No matches");
	private static final Failable<WithConfidence<RSSTorrent>> SIZE_NOT_REPORTED = Failable.fail("Cannot look up size");
	private static final String X264 = "X264";
	private static final int LOWER_FILE_1080P = 600;
	private static final int UPPER_FILE_1080P = 1200;
	private static final int LOWEST_FILE = 400;
	private final String pref;
	private final Logger logger;

	public PreferenceScrape(String p, Logger logger) {
		this.pref = p;
		this.logger = Preconditions.checkNotNull(logger);
	}

	public Collection<WithConfidence<RSSTorrent>> chooseBestMatch(ImmutableList<RSSTorrent> lastItems) {
		Collection<RSSTorrent> matches = findAllMatches(lastItems);
		if (matches.isEmpty()) {
			return Collections.emptyList();
		}

		Collection<WithConfidence<RSSTorrent>> out = Lists.newArrayListWithCapacity(1);
		Confidence lastConfidence = Confidence.LOW;
		Optional<WithConfidence<RSSTorrent>> lastMatch = Optional.absent();

		Failable<WithConfidence<RSSTorrent>> topRes = find1080pVersion(matches);
		if (topRes.isSuccess()) {
			lastConfidence = topRes.get().getConfidence();
			lastMatch = Optional.of(topRes.get());
		}

		if (lastConfidence == Confidence.HIGH) {
			out.add(lastMatch.get());
			return out;
		}

		Failable<WithConfidence<RSSTorrent>> medRes = find720pVersion(matches);
		if (medRes.isSuccess()) {
			lastMatch = Optional.of(getMostConfident(lastMatch, medRes.get()));
			lastConfidence = lastMatch.get().getConfidence();
		}

		if (lastConfidence == Confidence.HIGH) {
			out.add(lastMatch.get());
			return out;
		}

		Failable<WithConfidence<RSSTorrent>> lowRes = find480pVersion(matches);
		if (lowRes.isSuccess()) {
			lastMatch = Optional.of(getMostConfident(lastMatch, lowRes.get()));
			lastConfidence = lastMatch.get().getConfidence();
		}

		if (lastConfidence == Confidence.HIGH) {
			out.add(lastMatch.get());
			return out;
		}

		Failable<WithConfidence<RSSTorrent>> inRange = findInFileSizeRange(matches, LOWEST_FILE, UPPER_FILE_1080P);
		if (inRange.isSuccess()) {
			lastMatch = Optional.of(getMostConfident(lastMatch, inRange.get()));
			lastConfidence = lastMatch.get().getConfidence();
		}

		if (lastConfidence == Confidence.LOW) {
			out.add(WithConfidence.low(Iterables.getFirst(matches, null)));
			return out;
		}

		out.add(lastMatch.get());
		return out;
	}

	private WithConfidence<RSSTorrent> getMostConfident(Optional<WithConfidence<RSSTorrent>> lastMatch,
			WithConfidence<RSSTorrent> withConfidence) {
		if (lastMatch.isPresent()) {
			return WithConfidence.getHighest(lastMatch.get(), withConfidence);
		}
		return withConfidence;

	}

	@SuppressWarnings("unused")
	private Failable<WithConfidence<RSSTorrent>> findInFileSizeRange(Collection<RSSTorrent> matches, int i, int j) {
		return SIZE_NOT_REPORTED;
	}

	private Failable<WithConfidence<RSSTorrent>> find480pVersion(Collection<RSSTorrent> matches) {
		Collection<RSSTorrent> matches480p = doFindMatches(matches, P480);
		if (matches480p.isEmpty()) {
			return NO_MATCHES;
		}
		if (matches480p.size() == 1) {
			return Failable.ofSuccess(WithConfidence.high(matches480p.iterator().next()));
		}
		return tryX264OrFilesizeElseChooseArbitrarily(matches480p, LOWER_FILE_1080P, UPPER_FILE_1080P);
	}

	private Failable<WithConfidence<RSSTorrent>> find720pVersion(Collection<RSSTorrent> matches) {
		Collection<RSSTorrent> matches720p = doFindMatches(matches, P720);
		if (matches720p.isEmpty()) {
			return NO_MATCHES;
		}
		if (matches720p.size() == 1) {
			return Failable.ofSuccess(WithConfidence.high(matches720p.iterator().next()));
		}
		return tryX264OrFilesizeElseChooseArbitrarily(matches720p, LOWER_FILE_1080P, UPPER_FILE_1080P);
	}

	private Failable<WithConfidence<RSSTorrent>> find1080pVersion(Collection<RSSTorrent> matches) {
		Collection<RSSTorrent> matches1080p = doFindMatches(matches, P1080);
		if (matches1080p.isEmpty()) {
			return NO_MATCHES;
		}
		if (matches1080p.size() == 1) {
			return Failable.ofSuccess(WithConfidence.high(matches1080p.iterator().next()));
		}
		return tryX264OrFilesizeElseChooseArbitrarily(matches1080p, LOWER_FILE_1080P, UPPER_FILE_1080P);

	}

	private Failable<WithConfidence<RSSTorrent>> tryX264OrFilesizeElseChooseArbitrarily(Collection<RSSTorrent> matches,
			int lowerSize, int upperSize) {
		Collection<RSSTorrent> matchesX264 = doFindMatches(matches, X264);
		if (matchesX264.isEmpty()) {
			return tryFilesizeElseChooseArbitrarily(matches, lowerSize, upperSize);
		}
		return Failable.ofSuccess(WithConfidence.medium(Iterables.getFirst(matches, null)));
	}

	@SuppressWarnings("unused")
	private Failable<WithConfidence<RSSTorrent>> tryFilesizeElseChooseArbitrarily(Collection<RSSTorrent> matches,
			int lowerSize, int upperSize) {
		return Failable.ofSuccess(WithConfidence.medium(Iterables.getFirst(matches, null)));
	}

	private Collection<RSSTorrent> findAllMatches(ImmutableList<RSSTorrent> lastItems) {
		String s = pref;
		return doFindMatches(lastItems, s);
	}

	private Collection<RSSTorrent> doFindMatches(Collection<RSSTorrent> matches, String s) {
		Collection<RSSTorrent> allMatches = Lists.newArrayListWithExpectedSize(matches.size() / 2);
		for (RSSTorrent i : matches) {
			if (i.getName().toLowerCase().contains(s.toLowerCase())) {
				if (Matching.isWholeSeason(i.getName())) {
					continue;
				}
				allMatches.add(i);
			}
		}
		logger.debug("Found " + allMatches.size() + " matches for " + s);
		return allMatches;
	}
}
