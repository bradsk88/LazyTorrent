package ca.bradj.lazytorrent.rss;

public interface Extension {

	Void apply(DefaultRSSTorrent.Builder b, String extension);

}
