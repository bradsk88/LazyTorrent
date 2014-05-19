package ca.bradj.lazytorrent.app;

import java.util.Collection;

public interface Logger {

	void addLogListener(LogListener loggerDisplay);

	String getMessagesSoFar();

	Collection<String> getMessageBuffer();

	void notification(String string);

	void error(String string);

	void log(String string);

	void debug(String string);

	void clearMessageBuffer();

}
