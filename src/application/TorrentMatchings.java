package application;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Collection;

import javafx.collections.ObservableList;
import javafx.util.Pair;
import ca.bradj.common.base.Failable;
import ca.bradj.lazytorrent.prefs.DefaultPreferences;
import ca.bradj.lazytorrent.prefs.Preferences;
import ca.bradj.lazytorrent.prefs.Unmovables;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class TorrentMatchings {

	private final Preferences prefs;
	private final Unmovables unmovables;

	public TorrentMatchings(Preferences p, Unmovables u) {
		this.prefs = Preconditions.checkNotNull(p);
		this.unmovables = Preconditions.checkNotNull(u);
	}

	public TorrentMatch getStrongestMatch(String name) {
		Failable<Pair<String, Double>> strongestMatch = Matching.getStrongestMatch(prefs.getList(), name);
		if (strongestMatch.isSuccess()) {
			return DefaultTorrentMatch.ofPreference(strongestMatch.get());
		}
		Failable<Pair<String, Double>> strongestMatch2 = Matching.getStrongestMatch(unmovables.getList(), name);
		if (strongestMatch2.isSuccess()) {
			return DefaultTorrentMatch.ofUnmovable(strongestMatch2.get());
		}
		return DefaultTorrentMatch.unmatched(strongestMatch.getReason());
	}

	public Preferences getPreferences() {
		return this.prefs;
	}

	public static TorrentMatchings load(Path root) throws FileNotFoundException {
		Preferences p = DefaultPreferences.load(root);
		Unmovables u = Unmovables.load(root);
		return new TorrentMatchings(p, u);
	}

	public void addUnmovable(String name) {
		this.unmovables.addAndWriteToDisk(name);
	}

	public ObservableList<String> getUnmovables() {
		return unmovables.getObservableList();
	}

	public Collection<String> getFile() {
		return Lists.newArrayList(prefs.getFilename(), unmovables.getFilename());
	}

}
