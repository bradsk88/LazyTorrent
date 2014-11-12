package ca.bradj.lazytorrent.app;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Collection;

import ca.bradj.common.base.Failable;
import ca.bradj.gsmatch.Match;
import ca.bradj.gsmatch.Matching;
import ca.bradj.gsmatch.TorrentMatch;
import ca.bradj.lazytorrent.prefs.DefaultPreferences;
import ca.bradj.lazytorrent.prefs.Preferences;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class TorrentMatchings {

	private final Preferences prefs;

	public TorrentMatchings(Preferences p) {
		this.prefs = Preconditions.checkNotNull(p);
	}

	public TorrentMatch getStrongestMatch(String name) {
		Failable<Match> strongestMatch = Matching.getStrongestMatch(prefs.getList(), name);
		if (strongestMatch.isSuccess()) {
			return DefaultTorrentMatch.ofPreference(strongestMatch.get());
		}
		return DefaultTorrentMatch.unmatched(strongestMatch.getReason());
	}

	public Preferences getPreferences() {
		return this.prefs;
	}

	public static TorrentMatchings load(Path root) throws FileNotFoundException {
		Preferences p = DefaultPreferences.load(root);
		return new TorrentMatchings(p );
	}

	public Collection<String> getFile() {
		return Lists.newArrayList(prefs.getFilename());
	}

}
