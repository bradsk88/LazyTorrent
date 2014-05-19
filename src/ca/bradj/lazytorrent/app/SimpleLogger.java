package ca.bradj.lazytorrent.app;

import java.util.Collection;

import org.joda.time.DateTime;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;

public class SimpleLogger implements Logger {

	private final Collection<LogListener> listeners = Lists.newArrayList();
	private final EvictingQueue<String> messagesBuffer = EvictingQueue.create(1000);

	@Override
	public void log(String message) {
		for (LogListener l : listeners) {
			l.newMessageAdded(message);
		}
		messagesBuffer.add("[LOG-" + DateTime.now().toString("ddMMYY-hhmm") + "]" + message);
	}

	@Override
	public void addLogListener(LogListener loggerDisplay) {
		this.listeners.add(loggerDisplay);
	}

	@Override
	public String getMessagesSoFar() {
		StringBuilder sb = new StringBuilder();
		for (String i : messagesBuffer) {
			sb.append(i);
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public void notification(String string) {
		for (LogListener l : listeners) {
			l.newNotificationAdded(string);
		}
		messagesBuffer.add("[NOTIF-" + DateTime.now().toString("ddMMYY-hhmm") + "]" + string);
	}

	@Override
	public void error(String string) {
		for (LogListener l : listeners) {
			l.newErrorAdded(string);
		}
		messagesBuffer.add("[ERROR-" + DateTime.now().toString("ddMMYY-hhmm") + "]" + string);
	}

	@Override
	public void debug(String string) {
		for (LogListener l : listeners) {
			l.debugAdded(string);
		}
		System.out.println(string);
		messagesBuffer.add("[DEBUG-" + DateTime.now().toString("ddMMYY-hhmm") + "]" + string);
	}

	@Override
	public Collection<String> getMessageBuffer() {
		return Lists.newArrayList(messagesBuffer);
	}

	@Override
	public void clearMessageBuffer() {
		messagesBuffer.clear();
		for (LogListener l : listeners) {
			l.bufferCleared();
		}
	}

}
