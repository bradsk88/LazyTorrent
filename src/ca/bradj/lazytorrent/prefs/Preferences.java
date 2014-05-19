package ca.bradj.lazytorrent.prefs;

import java.util.Collection;

public interface Preferences {

	Collection<String> getList();

	String getFilename();

	boolean matches(String name);

	void addListener(RecordsChangeListener l);

	void addAndWriteToDisk(String text);

}
