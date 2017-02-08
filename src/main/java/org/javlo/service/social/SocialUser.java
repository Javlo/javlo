package org.javlo.service.social;

public class SocialUser {
	private String email;
	private String firstName;
	private String lastName;
	private String avatarURL;

	public String getEmail() {
		return email;
	}

	public SocialUser setEmail(String email) {
		this.email = email;
		return this;
	}

	public String getFirstName() {
		return firstName;
	}

	public SocialUser setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public String getLastName() {
		return lastName;
	}

	public SocialUser setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	public String getAvatarURL() {
		return avatarURL;
	}

	public SocialUser setAvatarURL(String avatarURL) {
		this.avatarURL = avatarURL;
		return this;
	}

}
