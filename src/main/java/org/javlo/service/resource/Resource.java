package org.javlo.service.resource;

public class Resource {

	private String id;
	private String uri;
	private String name;
	private String description;

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
			Resource otherResource = (Resource) obj;
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

	@Override
	public int hashCode() {
		if (getUri() == null) {
			return super.hashCode();
		} else {
			return getUri().hashCode();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
