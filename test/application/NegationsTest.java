package application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

import ca.bradj.lazytorrent.matching.Negation;
import ca.bradj.lazytorrent.matching.Negations;

import com.google.common.collect.Lists;

public class NegationsTest {

	@Test
	public void testPureSingleNegation() {
		String s = "-Word";
		Collection<Negation> collection = Negations.get(s);
		assertEquals("Expected " + s + " but was " + collection, 1, collection.size());
	}

	@Test
	public void testNoNegationsReturnsEmptyResult() {
		String s = "no negations";
		Collection<Negation> collection = Negations.get(s);
		assertTrue("Expected empty but was: " + collection, collection.isEmpty());
	}

	@Test
	public void testSecondWordNegatedButFirstWordNotResultsInSingleNegationExtracted() {
		String s = "word -negation";
		Collection<Negation> collection = Negations.get(s);
		assertEquals("Expected [-negation] but was: " + collection, collection.size(), 1);
	}

	@Test
	public void testCollectionContainingAllNegsIsEmptyAfterRemoved() {
		Collection<String> collection = Lists.newArrayList("-US");
		Collection<Negation> negs = Lists.newArrayList(Negation.of("US"));
		Collection<String> collection2 = Negations.removeNegations(negs, collection);
		assertTrue("Expected [] but was: " + collection2, collection2.isEmpty());
	}
}
