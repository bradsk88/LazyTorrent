package application;

import com.google.common.base.Preconditions;

public class Negation {

	private final String string;

	private Negation(String string) {
		this.string = Preconditions.checkNotNull(string);
	}

	public static Negation of(String string) {
		return new Negation(string);
	}

	public String get() {
		return string;
	}

	public boolean matches(String string2) {
		if (string2 == null) {
			return false;
		}
		String lc = string.toLowerCase();
		String lcIn = string2.toLowerCase();
		if (lc.equals(lcIn)) {
			return true;
		}
		if (lc.equals(lcIn.replace("-", ""))) {
			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		return "-" + string;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((string == null) ? 0 : string.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Negation other = (Negation) obj;
		if (string == null) {
			if (other.string != null) {
				return false;
			}
		} else if (!string.equals(other.string)) {
			return false;
		}
		return true;
	}

}
