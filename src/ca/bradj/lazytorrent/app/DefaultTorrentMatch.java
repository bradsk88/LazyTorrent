package ca.bradj.lazytorrent.app;

import ca.bradj.gsmatch.Match;
import ca.bradj.gsmatch.Negations;
import ca.bradj.gsmatch.TorrentMatch;
import javafx.util.Pair;

public class DefaultTorrentMatch implements TorrentMatch {

    private static final String NOPROB = "No problems encountered";

    private final String matchString;
    private final Double matchScore;
    private final TorrentMatchType type;

    private DefaultTorrentMatch(String key, Double value, TorrentMatchType unmovable) {
        this.matchString = key;
        this.matchScore = value;
        this.type = unmovable;
    }

    @Override
    public boolean isPreference() {
        return type == TorrentMatchType.PREF;
    }

    @Override
    public String getName() {
        return Negations.removeAllNegations(matchString);
    }

    @Override
    public boolean isUnmovable() {
        return type == TorrentMatchType.UNMOVABLE;
    }

    @Override
    public boolean isMovie() {
        return type == TorrentMatchType.MOVIE;
    }

    @Override
    public String getReason() {
        return NOPROB;
    }

    private static enum TorrentMatchType {
        PREF, UNMOVABLE, MOVIE, FAILURE;
    }

    /**
     * @deprecated Please use {@link #ofPreference(Match)}.
     */
    @Deprecated
    public static DefaultTorrentMatch ofPreference(Pair<String, Double> pair) {
        return new DefaultTorrentMatch(pair.getKey(), pair.getValue(), TorrentMatchType.PREF);
    }

    public static TorrentMatch ofPreference(Match match) {
        return new DefaultTorrentMatch(match.getName(), match.getPercentConfidence(), TorrentMatchType.PREF);
    }

    /**
     * @deprecated Please use {@link #ofUnmovable(Match)}.
     */
    @Deprecated
    public static DefaultTorrentMatch ofUnmovable(Pair<String, Double> pair) {
        return new DefaultTorrentMatch(pair.getKey(), pair.getValue(), TorrentMatchType.UNMOVABLE);
    }

    public static TorrentMatch ofUnmovable(Match match) {
        return new DefaultTorrentMatch(match.getName(), match.getPercentConfidence(), TorrentMatchType.UNMOVABLE);
    }

    public static TorrentMatch unmatched(String reason) {
        return new EmptyTorrentMatch(reason);
    }

    @Override
    public String toString() {
        return "DefaultTorrentMatch [matchString=" + matchString + ", matchScore=" + matchScore + ", type=" + type + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((matchScore == null) ? 0 : matchScore.hashCode());
        result = prime * result + ((matchString == null) ? 0 : matchString.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DefaultTorrentMatch other = (DefaultTorrentMatch) obj;
        if (matchScore == null) {
            if (other.matchScore != null) {
                return false;
            }
        } else if (!matchScore.equals(other.matchScore)) {
            return false;
        }
        if (matchString == null) {
            if (other.matchString != null) {
                return false;
            }
        } else if (!matchString.equals(other.matchString)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

}
