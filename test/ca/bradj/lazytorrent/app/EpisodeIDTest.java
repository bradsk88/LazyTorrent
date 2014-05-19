package ca.bradj.lazytorrent.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ca.bradj.common.base.Failable;
import ca.bradj.lazytorrent.app.EpisodeID;

public class EpisodeIDTest {

	@Test
	public void testPerfectlyFormattedString() {
		Failable<EpisodeID> parse = EpisodeID.parse("S01E10");
		assertTrue(parse.isSuccess());
		assertEquals(1, parse.get().getSeason());
		assertEquals(10, parse.get().getEpisode());
	}

}
