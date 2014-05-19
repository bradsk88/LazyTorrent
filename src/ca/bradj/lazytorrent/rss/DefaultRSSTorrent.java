package ca.bradj.lazytorrent.rss;

import com.google.common.base.Preconditions;

public class DefaultRSSTorrent implements RSSTorrent {

	public static class Builder {

		private String name;
		private String link;
		private String date;
		private String desc;

		public Builder name(String data) {
			this.name = data;
			return this;
		}

		public Builder link(String data) {
			this.link = data;
			return this;
		}

		public DefaultRSSTorrent build() {
			return new DefaultRSSTorrent(name, link, date, desc);
		}

		public Builder date(String data) {
			this.date = data;
			return this;
		}

		public Builder description(String data) {
			this.desc = data;
			return this;
		}

		public void linkExtend(String data) {
			this.link += data;
		}

		public void descriptionExtend(String data) {
			this.desc += data;
		}

		public void nameExtend(String extension) {
			this.name += extension;
		}
	}

	private final String name;
	private final String link;
	private final String date;
	private final String desc;

	public DefaultRSSTorrent(String name, String link, String date, String desc) {
		this.name = Preconditions.checkNotNull(name);
		this.link = Preconditions.checkNotNull(link);
		this.date = Preconditions.checkNotNull(date);
		this.desc = Preconditions.checkNotNull(desc);
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public String toUserString() {
		return name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getURL() {
		return link;
	}

	@Override
	public String toString() {
		return "DefaultRSSTorrent [name=" + name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((desc == null) ? 0 : desc.hashCode());
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		DefaultRSSTorrent other = (DefaultRSSTorrent) obj;
		if (date == null) {
			if (other.date != null) {
				return false;
			}
		} else if (!date.equals(other.date)) {
			return false;
		}
		if (desc == null) {
			if (other.desc != null) {
				return false;
			}
		} else if (!desc.equals(other.desc)) {
			return false;
		}
		if (link == null) {
			if (other.link != null) {
				return false;
			}
		} else if (!link.equals(other.link)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

}
