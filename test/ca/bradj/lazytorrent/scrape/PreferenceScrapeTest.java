package ca.bradj.lazytorrent.scrape;

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import javafx.util.Pair;

import org.junit.Test;
import org.mockito.Mockito;

import ca.bradj.common.base.WithConfidence;
import ca.bradj.lazytorrent.app.Logger;
import ca.bradj.lazytorrent.rss.RSSTorrent;

import com.google.common.collect.ImmutableList;

public class PreferenceScrapeTest {

	@Test
	public void testNOVADoesntMatchRayDonovan() {

		PreferenceScrape ps = new PreferenceScrape("NOVA", Mockito.mock(Logger.class));
		RSSTorrent raydono = Mockito.mock(RSSTorrent.class);
		Mockito.when(raydono.getName()).thenReturn("Ray.Donovan.S01E08.720p.BluRay.X264-REWARD");
		ImmutableList<RSSTorrent> lastItems = ImmutableList.of(raydono);
		Collection<WithConfidence<Pair<RSSTorrent, String>>> chooseBestMatch = ps.chooseBestMatch(lastItems);
		assertTrue(chooseBestMatch.isEmpty());
	}

}
