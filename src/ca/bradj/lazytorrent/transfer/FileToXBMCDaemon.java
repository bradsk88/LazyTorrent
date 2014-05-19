package ca.bradj.lazytorrent.transfer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import application.MoveFinishedTorrents;
import application.TorrentMatchings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import ca.bradj.lazytorrent.app.Logger;
import ca.bradj.lazytorrent.rss.FXThreading;

public class FileToXBMCDaemon {

	private final SimpleStringProperty countDownProperty = new SimpleStringProperty();
	private final ScheduledExecutorService moveEx = Executors.newScheduledThreadPool(1);
	private final ScheduledExecutorService countEx = Executors.newScheduledThreadPool(1);
	private Logger logger;
	private TorrentMatchings matchings;
	private AlreadyTransferred already;

	public ScheduledExecutorService start(Logger logger, TorrentMatchings matchings, AlreadyTransferred already) {
		this.logger = logger;
		this.matchings = matchings;
		this.already = already;

		logger.debug("Starting file transfer service");
		moveEx.scheduleAtFixedRate(moveTorrentsAndStartCountDown(), 0, 20, TimeUnit.MINUTES);
		return moveEx;
	}

	private Runnable moveTorrentsAndStartCountDown() {
		return new Runnable() {

			volatile ScheduledFuture<?> self;

			@Override
			public void run() {

				self = countEx.scheduleAtFixedRate(new Runnable() {

					final AtomicInteger seconds = new AtomicInteger(20 * 60);

					@Override
					public void run() {
						FXThreading.invokeLater(new Runnable() {
							@Override
							public void run() {
								countDownProperty.set(countdownWillBeginIn(seconds.get()));
							}
						});
						if (seconds.decrementAndGet() <= 0) {
							self.cancel(true);
						}
					}
				}, 0, 1, TimeUnit.SECONDS);

				try {
					new MoveFinishedTorrents(logger, matchings, already).run();

				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e.getMessage());
				}

			}
		};
	}

	protected String countdownWillBeginIn(int i) {
		return "File transfers will begin in " + i + " seconds.";
	}

	public ObservableValue<? extends String> countDownProperty() {
		return countDownProperty;
	}

}
