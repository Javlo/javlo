package org.javlo.user;

public class AdminUserInfo extends UserInfo {

	private String facebook;
	private String googleplus;
	private String linkedin;
	private String twitter;

	public String getFacebook() {
		return facebook;
	}

	public void setFacebook(String facebook) {
		this.facebook = facebook;
	}

	public String getGoogleplus() {
		return googleplus;
	}

	public void setGoogleplus(String googleplus) {
		this.googleplus = googleplus;
	}

	public String getLinkedin() {
		return linkedin;
	}

	public void setLinkedin(String linkedin) {
		this.linkedin = linkedin;
	}

	public String getTwitter() {
		return twitter;
	}

	public void setTwitter(String twitter) {
		this.twitter = twitter;
	}

	@Override
	public String getAvatarURL() {
		if (getFacebook() != null) {
			return getFacebook().replace("//www.", "//graph.") + "/picture?type=small";
		} else if (getTwitter() != null) {
			return "https://api.twitter.com/1/users/profile_image?screen_name=" + getTwitter().replaceAll("https://twitter.com/", "") + "&size=normal";
		} else {
			return null;
		}
	}

}
