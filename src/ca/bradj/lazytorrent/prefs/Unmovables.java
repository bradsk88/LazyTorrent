package ca.bradj.lazytorrent.prefs;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

public class Unmovables extends Record<Unmovables> {

	private static final String THISDIR = "unmovables";

	public Unmovables(Path root, Collection<String> livePrefs2) {
		super(root, THISDIR, livePrefs2);
	}

	public static Unmovables load(final Path root) throws FileNotFoundException {
		File f = new File(root + File.separator + THISDIR);

		if (f.exists()) {
			if (Record.randomlyPurge()) {
				f.delete();
				return Unmovables.empty(root);
			}
			return new Unmovables(root, Record.parse(f));
		}
		return Unmovables.empty(root);
	}

	@SuppressWarnings("unchecked")
	private static Unmovables empty(Path root) {
		return new Unmovables(root, Collections.EMPTY_LIST);
	}

}
