package ca.bradj.lazytorrent.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.bradj.common.base.Failable;
import ca.bradj.lazytorrent.connection.Torrent;

//EpisodeIDTest
public class EpisodeID {

	private static final Pattern PATTERN = Pattern.compile("[^0-9]*0*([0-9]+)[[xX]|[[eE][^0-9]*]]0*([0-9]+).*");
	private static final Pattern PATTERN_SEASON = Pattern.compile(".*[^a-zA-Z][sS][^0-9]*([0-9][0-9]?)[^0-9]+.*");
	private static final Failable<EpisodeID> NO_MATCH = Failable.fail("Could not find season pattern (S##E##)");
	private final int season;
	private final int episode;

	private EpisodeID(int season, int episode) {
		this.season = season;
		this.episode = episode;
	}

	public static Failable<EpisodeID> parse(String alreadyDownloaded) {
		String[] split = alreadyDownloaded.split("\\.");

		for (String sub : split) {
			Matcher matcher = PATTERN.matcher(sub);
			if (matcher.matches()) {
				int season = Integer.parseInt(matcher.group(1));
				int episode = Integer.parseInt(matcher.group(2));
				EpisodeID epid = EpisodeID.withSeason(season).andEpisode(episode);
				return Failable.ofSuccess(epid);
			}
		}
		return NO_MATCH;
	}

	public static class IntermediateStep {

		private final int season;

		public IntermediateStep(int season) {
			this.season = season;
		}

		public EpisodeID andEpisode(int episode) {
			return new EpisodeID(season, episode);
		}

	}

	private static IntermediateStep withSeason(int season) {
		return new IntermediateStep(season);
	}

	public int getSeason() {
		return season;
	}

	public int getEpisode() {
		return episode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + episode;
		result = prime * result + season;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EpisodeID other = (EpisodeID) obj;
		if (episode != other.episode)
			return false;
		if (season != other.season)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EpisodeID [season=" + season + ", episode=" + episode + "]";
	}

	public static boolean isA(String i) {
		return PATTERN.matcher(i).matches();
	}

	public static boolean isWholeSeason(String name) {
		String bareName = Torrent.stripJunk(name);
		Failable<EpisodeID> parse = parse(bareName);
		if (parse.isSuccess()) {
			return false;
		}
		boolean matches = PATTERN_SEASON.matcher(bareName).matches();
		return matches;

	}

}
