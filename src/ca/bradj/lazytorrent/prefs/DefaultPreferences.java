package ca.bradj.lazytorrent.prefs;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

public class DefaultPreferences extends Record<DefaultPreferences> implements Preferences {

	private static final String THISDIR = "prefs";

	DefaultPreferences(Path root, Collection<String> livePrefs) {
		super(root, THISDIR, livePrefs);
	}

	public static DefaultPreferences load(Path rootDir) throws FileNotFoundException {
		File f = new File(rootDir + File.separator + THISDIR);
		if (f.exists()) {
			return new DefaultPreferences(rootDir, Record.parse(f));
		}
		return DefaultPreferences.empty(rootDir);
	}

	@SuppressWarnings("unchecked")
	private static DefaultPreferences empty(Path root) {
		return new DefaultPreferences(root, Collections.EMPTY_LIST);
	}

}
