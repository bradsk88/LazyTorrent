package ca.bradj.lazytorrent.matching;

import java.util.Collection;

import javafx.util.Pair;
import ca.bradj.common.base.Failable;
import ca.bradj.lazytorrent.app.EpisodeID;
import ca.bradj.lazytorrent.connection.Torrent;

import com.google.common.collect.Iterables;

public class Matching {

	public static Failable<Pair<String, Double>> getStrongestMatch(Collection<String> list, String name) {
		Failable<Pair<String, Double>> result = Failable.fail("Could not find match");
		for (String i : list) {
			Failable<Double> percent = getMatchPercent(name, i);
			if (!result.isSuccess()) {
				if (percent.isSuccess()) {
					result = Failable.ofSuccess(new Pair<String, Double>(i.trim(), percent.get()));
				}
				continue;
			}
			if (percent.isSuccess() && percent.get() > result.get().getValue()) {
				result = Failable.ofSuccess(new Pair<>(i.trim(), percent.get()));
			}

		}
		return result;
	}

	public static Failable<Double> getMatchPercent(String toMatch, String against) {
		double percent = getNameOverlap(toMatch, against);
		if (percent > 0.75) {
			return Failable.ofSuccess(percent);
		}
		if (percent >= 0.25) {
			boolean epidMatches = doesEpisodeIdMatch(toMatch, against);
			if (epidMatches) {
				// it's going to be very rare that two shows having name 25%
				// similar will be broadcasting the same episode and season at
				// the same time. This is probably a match.
				return Failable.ofSuccess(percent);
			}
		}
		return Failable.fail("Not a strong enough match between " + toMatch + " and " + against + " (" + percent * 100
				+ "%)");
	}

	public static double getNameOverlap(String toMatch, String against) {
		Collection<String> inPotentialNames = Torrent.getPotentialName(toMatch);
		Collection<String> adPotentialNames = Torrent.getPotentialName(against);
		Collection<Negation> negations = Negations.get(against);
		adPotentialNames = Negations.removeNegations(negations, adPotentialNames);
		double percent = calculatePercentageOverlap(negations, inPotentialNames, adPotentialNames);
		return percent;
	}

	private static double calculatePercentageOverlap(Collection<Negation> negations,
			Collection<String> inPotentialNames, Collection<String> adPotentialNames) {
		int minSize = Math.min(inPotentialNames.size(), adPotentialNames.size());
		double baseScore = 1.0 / minSize;
		double halfScore = baseScore / 2;
		double score = 0;
		for (String i : inPotentialNames) {
			if (Negations.matches(negations, i)) {
				return -1;
			}
		}
		for (int i = 0; i < minSize; i++) {
			String string = Iterables.get(inPotentialNames, i);
			String anObject = Iterables.get(adPotentialNames, i);
			if (string.toLowerCase().equals(anObject.toLowerCase())) {
				score += baseScore;
				continue;
			}
			baseScore = halfScore; // after first miss, halve scores for
									// matches.
		}
		return score;
	}

	private static boolean doesEpisodeIdMatch(String inString, String alreadyDownloaded) {
		Failable<EpisodeID> inID = EpisodeID.parse(inString);
		Failable<EpisodeID> adID = EpisodeID.parse(alreadyDownloaded);
		if (inID.isSuccess() && adID.isSuccess()) {
			if (inID.get().equals(adID.get())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isWholeSeason(String name) {
		return EpisodeID.isWholeSeason(name);
	}
}
