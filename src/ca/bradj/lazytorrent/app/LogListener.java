package ca.bradj.lazytorrent.app;

public interface LogListener {

	void newMessageAdded(String message);

	void newNotificationAdded(String string);

	void newErrorAdded(String string);

	void bufferCleared();

	void debugAdded(String string);

}
