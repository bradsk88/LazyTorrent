package ca.bradj.lazytorrent.scrape;

import java.util.Collection;
import java.util.Collections;

import javafx.util.Pair;
import ca.bradj.common.base.Confidence;
import ca.bradj.common.base.Failable;
import ca.bradj.common.base.WithConfidence;
import ca.bradj.lazytorrent.app.Logger;
import ca.bradj.lazytorrent.matching.Matching;
import ca.bradj.lazytorrent.rss.RSSTorrent;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

//PreferenceScrapeTest
public class PreferenceScrape {

	private static final String P480 = "480p";
	private static final String P720 = "720p";
	private static final String P1080 = "1080p";
	private static final Failable<WithConfidence<Pair<RSSTorrent, String>>> NO_MATCHES = Failable.fail("No matches");
	private static final Failable<WithConfidence<Pair<RSSTorrent, String>>> SIZE_NOT_REPORTED = Failable
			.fail("Cannot look up size");
	private static final String X264 = "X264";
//	private static final int LOWER_FILE_1080P = 600;
//	private static final int UPPER_FILE_1080P = 1200;
//	private static final int LOWEST_FILE = 400;
	private final String pref;
	private final Logger logger;

	public PreferenceScrape(String p, Logger logger) {
		this.pref = p;
		this.logger = Preconditions.checkNotNull(logger);
	}

	public Collection<WithConfidence<Pair<RSSTorrent, String>>> chooseBestMatch(ImmutableList<RSSTorrent> lastItems) {
		ImmutableList<Pair<RSSTorrent, String>> pairWithPref = pairWithPref(lastItems, pref);
		Collection<Pair<RSSTorrent, String>> matches = findAllMatches(pairWithPref);
		if (matches.isEmpty()) {
			return Collections.emptyList();
		}

		Collection<WithConfidence<Pair<RSSTorrent, String>>> out = Lists.newArrayListWithCapacity(1);
		Confidence lastConfidence = Confidence.LOW;
		Optional<WithConfidence<Pair<RSSTorrent, String>>> lastMatch = Optional.absent();

		Failable<WithConfidence<Pair<RSSTorrent, String>>> topRes = find1080pVersion(matches);
		if (topRes.isSuccess()) {
			lastConfidence = topRes.get().getConfidence();
			lastMatch = Optional.of(topRes.get());
		}

		if (lastConfidence == Confidence.HIGH) {
			out.add(lastMatch.get());
			return out;
		}

		Failable<WithConfidence<Pair<RSSTorrent, String>>> medRes = find720pVersion(matches);
		if (medRes.isSuccess()) {
			lastMatch = Optional.of(getMostConfident(lastMatch, medRes.get()));
			lastConfidence = lastMatch.get().getConfidence();
		}

		if (lastConfidence == Confidence.HIGH) {
			out.add(lastMatch.get());
			return out;
		}

		Failable<WithConfidence<Pair<RSSTorrent, String>>> lowRes = find480pVersion(matches);
		if (lowRes.isSuccess()) {
			lastMatch = Optional.of(getMostConfident(lastMatch, lowRes.get()));
			lastConfidence = lastMatch.get().getConfidence();
		}

		if (lastConfidence == Confidence.HIGH) {
			out.add(lastMatch.get());
			return out;
		}

		Failable<WithConfidence<Pair<RSSTorrent, String>>> inRange = findInFileSizeRange();
		if (inRange.isSuccess()) {
			lastMatch = Optional.of(getMostConfident(lastMatch, inRange.get()));
			lastConfidence = lastMatch.get().getConfidence();
		}

		if (lastConfidence == Confidence.LOW) {
			Pair<RSSTorrent, String> first = Iterables.getFirst(matches, null);
			out.add(WithConfidence.low(first));
			return out;
		}

		out.add(lastMatch.get());
		return out;
	}

	private ImmutableList<Pair<RSSTorrent, String>> pairWithPref(ImmutableList<RSSTorrent> lastItems, String pref2) {
		Builder<Pair<RSSTorrent, String>> builder = ImmutableList.builder();
		for (RSSTorrent i : lastItems) {
			builder.add(new Pair<>(i, pref2));
		}
		return builder.build();
	}

	private WithConfidence<Pair<RSSTorrent, String>> getMostConfident(
			Optional<WithConfidence<Pair<RSSTorrent, String>>> lastMatch,
			WithConfidence<Pair<RSSTorrent, String>> withConfidence) {
		if (lastMatch.isPresent()) {
			return WithConfidence.getHighest(lastMatch.get(), withConfidence);
		}
		return withConfidence;

	}

	private Failable<WithConfidence<Pair<RSSTorrent, String>>> findInFileSizeRange() {
		return SIZE_NOT_REPORTED;
	}

	private Failable<WithConfidence<Pair<RSSTorrent, String>>> find480pVersion(
			Collection<Pair<RSSTorrent, String>> matches) {
		Collection<Pair<RSSTorrent, String>> matches480p = doFindMatches(matches, P480);
		if (matches480p.isEmpty()) {
			return NO_MATCHES;
		}
		if (matches480p.size() == 1) {
			Pair<RSSTorrent, String> next = matches480p.iterator().next();
			return Failable.ofSuccess(WithConfidence.high(next));
		}
		return tryX264OrFilesizeElseChooseArbitrarily(matches480p);
	}

	private Failable<WithConfidence<Pair<RSSTorrent, String>>> find720pVersion(
			Collection<Pair<RSSTorrent, String>> matches) {
		Collection<Pair<RSSTorrent, String>> matches720p = doFindMatches(matches, P720);
		if (matches720p.isEmpty()) {
			return NO_MATCHES;
		}
		if (matches720p.size() == 1) {
			Pair<RSSTorrent, String> next = matches720p.iterator().next();
			return Failable.ofSuccess(WithConfidence.high(next));
		}
		return tryX264OrFilesizeElseChooseArbitrarily(matches720p);
	}

	private Failable<WithConfidence<Pair<RSSTorrent, String>>> find1080pVersion(
			Collection<Pair<RSSTorrent, String>> matches) {
		Collection<Pair<RSSTorrent, String>> matches1080p = doFindMatches(matches, P1080);
		if (matches1080p.isEmpty()) {
			return NO_MATCHES;
		}
		if (matches1080p.size() == 1) {
			Pair<RSSTorrent, String> next = matches1080p.iterator().next();
			return Failable.ofSuccess(WithConfidence.high(next));
		}
		return tryX264OrFilesizeElseChooseArbitrarily(matches1080p);

	}

	private Failable<WithConfidence<Pair<RSSTorrent, String>>> tryX264OrFilesizeElseChooseArbitrarily(
			Collection<Pair<RSSTorrent, String>> matches) {
		Collection<Pair<RSSTorrent, String>> matchesX264 = doFindMatches(matches, X264);
		if (matchesX264.isEmpty()) {
			return tryFilesizeElseChooseArbitrarily(matches);
		}
		Pair<RSSTorrent, String> first = Iterables.getFirst(matches, null);
		return Failable.ofSuccess(WithConfidence.medium(first));
	}

	private Failable<WithConfidence<Pair<RSSTorrent, String>>> tryFilesizeElseChooseArbitrarily(
			Collection<Pair<RSSTorrent, String>> matches) {
		Pair<RSSTorrent, String> first = Iterables.getFirst(matches, null);
		return Failable.ofSuccess(WithConfidence.medium(first));
	}

	private Collection<Pair<RSSTorrent, String>> findAllMatches(ImmutableList<Pair<RSSTorrent, String>> lastItems) {
		String s = pref;
		return doFindMatches(lastItems, s);
	}

	private Collection<Pair<RSSTorrent, String>> doFindMatches(Collection<Pair<RSSTorrent, String>> matches, String s) {
		Collection<Pair<RSSTorrent, String>> allMatches = Lists.newArrayListWithExpectedSize(matches.size() / 2);
		for (Pair<RSSTorrent, String> i : matches) {
			String lOther = i.getKey().getName().toLowerCase();
			String lPref = s.toLowerCase();
			if (lOther.startsWith(lPref) || lOther.endsWith(lPref)) {
				if (Matching.isWholeSeason(i.getKey().getName())) {
					continue;
				}
				allMatches.add(i);
			}
		}
		logger.debug("Found " + allMatches.size() + " matches for " + s);
		return allMatches;
	}

	@Override
	public String toString() {
		return "PreferenceScrape [pref=" + pref + ", logger=" + logger + "]";
	}

}
