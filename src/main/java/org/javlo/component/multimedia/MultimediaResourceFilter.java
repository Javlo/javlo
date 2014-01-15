package org.javlo.component.multimedia;

public class MultimediaResourceFilter {

	private String query = null;

	public boolean accept(MultimediaResource resource) {
		if (resource == null) {
			return false;
		} else {
			if (query == null || query.length() == 0) {
				return true;
			} else {
				return resource.getFullDescription().contains(query);
			}
		}
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}
