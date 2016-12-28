package org.javlo.user;

import java.util.Date;
import java.util.Set;

import javax.mail.internet.InternetAddress;

public interface IUserInfo {

	public static final char ROLES_SEPARATOR = ';';
	public static final String PREFERRED_LANGUAGE_SEPARATOR = ",";

	public String getLogin();
	
	/**
	 * return the encrypt login the encrypt login can change when we restart. 
	 */
	public String getEncryptLogin();
	
	/**
	 * get the parent of the user, a parent can be a manager.
	 * @return
	 */
	public String getParent();

	public String getTitle();

	public String getFirstName();

	public String getLastName();
	
	public String getGender();

	public String getPassword();

	public String getEmail();
	
	public String getUrl();

	public String getInfo();

	public String getToken();
	
	public String getBirthdate();

	public String[] getPreferredLanguage();

	public Set<String> getRoles();

	public void setId(String id);

	public void setLogin(String login);

	public void setTitle(String title);

	public void setFirstName(String firstName);

	public void setLastName(String lastName);
	
	/**
	 * set password and encrypt if needed&²
	 * @param globalContext
	 * @param password
	 */
	public void setPassword(boolean encrypt, String password);

	public void setPassword(String password);

	public void setEmail(String email);

	public void setInfo(String info);

	public void setToken(String token);
	
	public void setPreferredLanguage(String[] preferredLanguage);

	public void setRoles(Set<String> strings);

	public void addRoles(Set<String> strings);

	public void removeRoles(Set<String> strings);

	public void setCreationDate(Date creationDate);

	public Date getCreationDate();

	public void setModificationDate(Date modificationDate);

	public Date getModificationDate();

	public String[] getAllLabels();

	public String[] getAllValues();
	
	public String getAvatarURL();
	
	public String getUserFolder();
	
	public InternetAddress getInternetAddress();

	/**
	 * get the type of account (default, facebook, google account...)
	 * @return
	 */
	public String getAccountType();

	/**
	 * define user as outside loged, as facebook or google login.
	 */
	void setExternalLoginUser();

	/**
	 * check if user is logged from external module (as facebook ou google).
	 * @return true if user logged from external module.
	 */
	boolean isExternalLoginUser();

}
