package ca.bradj.lazytorrent.prefs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Random;
import java.util.SortedSet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ca.bradj.lazytorrent.app.Config;
import ca.bradj.lazytorrent.rss.FXThreading;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class Record<R extends Record<R>> {
	// protected static final String DIR = System.getenv("APPDATA") +
	// File.separator + "LazyTorrent";
	private final SortedSet<String> livePrefs;
	private final Collection<RecordsChangeListener> listeners = Lists.newArrayList();
	private final String recordsPath;
	private final ObservableList<String> list;
	private final Path root;

	public Record(Path root, String recordPath, Collection<String> livePrefs2) {
		this.root = Preconditions.checkNotNull(root);
		this.livePrefs = Sets.newTreeSet(livePrefs2);
		this.livePrefs.remove("");
		this.recordsPath = root + File.separator + recordPath;
		this.list = FXCollections.observableArrayList(livePrefs2);
		addListener(new RecordsChangeListener() {

			@Override
			public void preferenceAdded(final String text) {
				FXThreading.invokeLater(new Runnable() {
					@Override
					public void run() {
						list.add(text);
					}
				});
			}
		});
	}

	public void addAndWriteToDisk(String text) {
		if (livePrefs.add(text)) {
			updateListeners(text);
			writePrefToFile(text);
		}
	}

	private void updateListeners(String text) {
		for (RecordsChangeListener l : listeners) {
			l.preferenceAdded(text);
		}
	}

	public void addListener(RecordsChangeListener listener) {
		this.listeners.add(listener);
	}

	public Collection<String> getList() {
		return livePrefs;
	}

	protected void writePrefToFile(String text) {

		File file = this.root.toFile();
		if (!file.exists()) {
			file.mkdirs();
		}

		File prefs = new File(recordsPath);
		String toAppend = text;
		if (prefs.exists()) {
			toAppend = "\n" + text;
		}
		try (FileWriter fr = new FileWriter(prefs, true)) {
			fr.write(toAppend);
		} catch (IOException e) {
			e.printStackTrace();

		}
	}

	public boolean matches(String name) {
		for (String i : livePrefs) {
			String iL = i.toLowerCase().substring(0, i.length());
			String nameL = name.toLowerCase();
			if (nameL.startsWith(iL) || nameL.endsWith(iL)) {
				return true;
			}
			if (i.length() > 4) {
				String iLowMinusExt = i.toLowerCase().substring(0, i.length() - 4);
				if (nameL.startsWith(iLowMinusExt) || nameL.endsWith(iLowMinusExt)) {
					return true;
				}
			}
		}
		return false;
	}

	public ObservableList<String> getObservableList() {
		return list;
	}

	public static Collection<String> parse(File f) {
		Collection<String> out = Lists.newArrayList();
		try (FileReader fr = new FileReader(f); BufferedReader br = new BufferedReader(fr)) {
			while (true) {
				String readLine = br.readLine();
				if (readLine == null) {
					return out;
				}
				out.add(readLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("File could not be parsed:" + f.getAbsolutePath());

	}

	@Override
	public String toString() {
		return "Record [livePrefs=" + livePrefs + ", listeners=" + listeners + ", recordsPath=" + recordsPath
				+ ", list=" + list + "]";
	}

	public String getFilename() {
		return recordsPath;
	}

	protected static boolean randomlyPurge() {
		if (!Config.ALLOW_RECORD_RESTART) {
			return false;
		}
		int num = new Random().nextInt(10);
		return num == 0;
	}

}
