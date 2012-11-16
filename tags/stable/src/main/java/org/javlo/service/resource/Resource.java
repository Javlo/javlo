package org.javlo.service.resource;

public class Resource {
	
	private String id;
	private String uri;
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Resource)) {
			return super.equals(obj);
		} else {
			Resource otherResource = (Resource)obj;
			if (otherResource.getUri() != null) {
				if (!otherResource.getUri().equals(getUri())) {
					return false;
				}
			} else {
				if (getUri() != null) {
					return false;
				}
			}
			if (otherResource.getId() != null) {
				if (!otherResource.getId().equals(getId())) {
					return false;
				}
			} else {
				if (getId() != null) {
					return false;
				}
			}
			return true;
		}
	}

}
