package ca.bradj.lazytorrent.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ca.bradj.common.base.Failable;

public class EpisodeIDTest {

	@Test
	public void testPerfectlyFormattedString() {
		Failable<EpisodeID> parse = EpisodeID.parse("S01E10");
		assertTrue(parse.isSuccess());
		assertEquals(1, parse.get().getSeason());
		assertEquals(10, parse.get().getEpisode());
	}

	@Test
	public void testShowWithSmushedEpisodeID() {
		Failable<EpisodeID> parse = EpisodeID.parse("community.507.xvid.h264");
		assertTrue(parse.isSuccess());
		assertEquals(5, parse.get().getSeason());
		assertEquals(7, parse.get().getEpisode());
	}

	@Test
	public void testShowWithWellFormedEpisodeID() {
		Failable<EpisodeID> parse = EpisodeID.parse("Orphan.Black.S02E07.720p.HDTV.X264-DIMENSION");
		assertTrue(parse.isSuccess());
		assertEquals(2, parse.get().getSeason());
		assertEquals(7, parse.get().getEpisode());
	}

	@Test
	public void testTagBloatedTitleIsNotMisinterpretedAsEpisodeID() {
		Failable<EpisodeID> parse = EpisodeID.parse("craig.ferguson.2014.01.17.chris.pine.720p.hdtv.x264-2hd");
		assertTrue(parse.isFailure());
	}

	@Test
	public void testEpisodeNumberAtStart() {
		Failable<EpisodeID> parse = EpisodeID.parse("1-01 The Adventures of Pete and Pete - King of the Road.avi");
		assertTrue(parse.isSuccess());
		assertEquals(1, parse.get().getSeason());
		assertEquals(1, parse.get().getEpisode());
	}

}
