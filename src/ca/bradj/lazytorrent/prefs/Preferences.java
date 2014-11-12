package ca.bradj.lazytorrent.prefs;

import java.util.Collection;

import ca.bradj.RecordsChangeListener;

public interface Preferences {

	Collection<String> getList();

	String getFilename();

	boolean matches(String name);

	void addListener(RecordsChangeListener l);

	void addAndWriteToDisk(String text);

}
