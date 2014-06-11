package ca.bradj.lazytorrent.app;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.junit.Test;

import ca.bradj.lazytorrent.rss.DefaultRSSTorrent;
import ca.bradj.lazytorrent.rss.DefaultRSSTorrent.Builder;
import ca.bradj.lazytorrent.rss.RSSTorrent;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AlreadyDownloadedTest {

	@Test
	public void test100percentMatch() {

		AlreadyDownloaded ad = make(Lists.newArrayList("Community.S05E04.480p.HDTV.x264-mSD"));
		RSSTorrent in = preparedBuilder().name("Community.S05E04.480p.HDTV.x264-mSD").build();
		assertTrue(ad.hasStrongMatch(in));

	}

	@Test
	public void testSimilarMatch() {

		AlreadyDownloaded ad = make(Lists.newArrayList("Community.S05E04.480p.HDTV.x264-mSD"));
		RSSTorrent in = preparedBuilder().name("Community.S05E04.720p.HDTV.X264-DIMENSION").build();
		assertTrue(ad.hasStrongMatch(in));

	}

	@Test
	public void testSameShowDifferentEpisodeShouldntMatch() {
		AlreadyDownloaded ad = make(Lists.newArrayList("Community.S05E04.480p.HDTV.x264-mSD"));
		RSSTorrent in = preparedBuilder().name(
				"Community.S05E03.Basic.Intergluteal.Numismatics.720p.WEB-DL.DD5.1.H.264-BS").build();
		assertFalse(ad.hasStrongMatch(in));
	}

	@Test
	public void testDifferentShowSimilarName() {
		AlreadyDownloaded ad = make(Lists.newArrayList("Community.S05E04.480p.HDTV.x264-mSD"));
		RSSTorrent in = preparedBuilder().name("Community.Sex.2013.1080p.Blu-ray.AVC.DD5.1").build();
		assertFalse(ad.hasStrongMatch(in));
	}

	@Test
	public void testSameDailyShowDifferentDay() {
		AlreadyDownloaded ad = make(Lists
				.newArrayList("The.Daily.Show.2014.01.15.Robert.Gates.REPACK.480p.HDTV.x264-mSD"));
		RSSTorrent in = preparedBuilder().name("The.Daily.Show.2014.01.14.Tim.Gunn.480p.HDTV.x264-mSD.").build();
		assertFalse(ad.hasStrongMatch(in));
	}

	@Test
	public void testSameDailyShowDifferentDay2() {
		AlreadyDownloaded ad = make(Lists.newArrayList("The.Daily.Show.2014.01.15.Robert.Gates.480p.HDTV.x264-mSD"));
		RSSTorrent in = preparedBuilder().name("The.Daily.Show.2014.01.14.Tim.Gunn.480p.HDTV.x264-mSD.").build();
		assertFalse(ad.hasStrongMatch(in));
	}

	@Test
	public void testSameDailyShowDifferentRelease() {
		AlreadyDownloaded ad = make(Lists.newArrayList("The.Daily.Show.2014.01.14.Tim.Gunn.HDTV.XviD-AFG."));
		RSSTorrent in = preparedBuilder().name("The.Daily.Show.2014.01.14.Tim.Gunn.480p.HDTV.x264-mSD.").build();
		assertTrue(ad.hasStrongMatch(in));
	}

	@Test
	public void testSameTopGearButOneTreatedAsOneWord() {
		AlreadyDownloaded ad = make(Lists.newArrayList("Top Gear 21x04 HDTV x264-FoV"));
		RSSTorrent in = preparedBuilder().name("Top Gear S21E04 PROPER HDTV x264-RiVER").build();
		assertTrue(ad.hasStrongMatch(in));
	}

	private AlreadyDownloaded make(ArrayList<String> newArrayList) {
		Map<Integer, Collection<String>> map = Maps.newHashMap();
		map.put(AlreadyDownloaded.SEEDING_DATE, newArrayList);
		return AlreadyDownloaded.with(map);
	}

	private Builder preparedBuilder() {
		return DefaultRSSTorrent.builder().link("").date("").description("");
	}
}
