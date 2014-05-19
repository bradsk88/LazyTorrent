package application;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

//NegationsTest
public class Negations {

	public static Collection<Negation> get(String toMatch) {
		if (!toMatch.contains("-")) {
			return Collections.emptyList();
		}
		String[] split = toMatch.split("-");
		Collection<Negation> negations = Lists.newArrayList();
		if (split.length >= 2) {
			if (toMatch.startsWith("-")) {
				negations.add(Negation.of(split[1]));
				if (split.length == 2) {
					return negations;
				}
			}
		}

		for (int i = 1; i < split.length; i++) {
			String sp = split[i];
			String[] split2 = sp.split(" ");
			if (split2.length > 0) {
				if (split2[0].isEmpty()) {
					continue;
				}
				negations.add(Negation.of(split2[0]));
			}
		}
		return negations;
	}

	public static boolean matches(Collection<Negation> negations, String string) {
		for (Negation i : negations) {
			if (i.matches(string)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNegation(String anObject) {
		if (anObject == null) {
			return false;
		}
		return anObject.startsWith("-");
	}

	public static Collection<String> removeNegations(Collection<Negation> negs, Collection<String> names) {
		Collection<String> out = Lists.newArrayList();
		for (String i : names) {
			for (String j : i.split(" ")) {
				if (Negations.matches(negs, j)) {
					continue;
				}
				out.add(j);
			}
		}
		return out;
	}

	public static List<String> removeNegations(Collection<Negation> negs, String name) {
		List<String> out = Lists.newArrayList();
		for (String j : name.split(" ")) {
			if (Negations.matches(negs, j)) {
				continue;
			}
			out.add(j);
		}
		return out;
	}

	public static String removeAllNegations(String matchString) {
		return Joiner.on(" ").join(removeNegations(get(matchString), matchString));
	}

}
