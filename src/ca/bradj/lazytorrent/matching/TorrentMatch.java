package ca.bradj.lazytorrent.matching;

public interface TorrentMatch {

	boolean isMovie();

	String getReason();

	boolean isUnmovable();

	String getName();

	boolean isPreference();

}
