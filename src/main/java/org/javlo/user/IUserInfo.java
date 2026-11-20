package org.javlo.user;

import jakarta.mail.internet.InternetAddress;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public interface IUserInfo {

	public static final char ROLES_SEPARATOR = ';';
	public static final String PREFERRED_LANGUAGE_SEPARATOR = ",";

	/**
	 * secret fields : never set from a request (form, import) and never exported.
	 * 'token' : internal token (pdf rendering, mailing link), 'accessToken' : hash of the bearer access token.
	 */
	public static final Set<String> SECRET_FIELDS = Set.of("token", "accessToken", "accessTokenDate");

	/**
	 * @return all the fields of the user info without the secret fields (for display, form prefill...).
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> toPublicMap(IUserInfo userInfo) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Map<String, String> map = org.javlo.helper.BeanHelper.bean2Map(userInfo);
		for (String secretField : SECRET_FIELDS) {
			map.remove(secretField);
		}
		return map;
	}

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

	public String getNationalRegister();

	public void setNationalRegister(String nationalRegister);

	public String getEmail();
	
	public String getUrl();

	public String getInfo();

	public String getToken();
	
	/**
	 * internal token, created if not exist (not a getter : must never be listed as a field, exported or stored in clear).
	 */
	public String tokenCreateIfNotExist();
	
	public void resetToken();

	/**
	 * @return the hash of the access token (never the clear token).
	 */
	public String getAccessToken();

	/**
	 * set the stored hash of the access token (used by the persistence), a clear value is refused.
	 */
	public void setAccessToken(String accessTokenHash);

	/**
	 * @return creation date of the access token (rendered with StringHelper.renderTime).
	 */
	public String getAccessTokenDate();

	public void setAccessTokenDate(String date);

	/**
	 * create a new access token (the previous one is revoked).
	 * @return the clear token, it is not stored and can not be retrieved later.
	 */
	public String createAccessToken();

	public void removeAccessToken();

	/**
	 * @param lifeTimeDays life time of the token (days), &lt;= 0 for no expiration.
	 * @return true if the clear token match the stored access token and is not expired.
	 */
	public boolean checkAccessToken(String clearToken, int lifeTimeDays);

	/**
	 * @return expiration date of the access token, null if no access token or no expiration.
	 */
	public Date accessTokenExpirationDate(int lifeTimeDays);

	public String getBirthdate();
	
	public String[] getPreferredLanguage();

	public Set<String> getRoles();

	String getAddress();

	void setAddress(String address);

	String getCity();

	void setCity(String city);

	String getPostCode();

	void setPostCode(String postCode);

	public void setId(String id);

	public void setLogin(String login);

	public void setTitle(String title);

	public void setFirstName(String firstName);

	public void setLastName(String lastName);
	
	public void setPassword(String password);

	public void setEmail(String email);

	public void setInfo(String info);

	public void setToken(String token);

	public void setBirthdate(String date);
	
	public void setPreferredLanguage(String[] preferredLanguage);

	public void setRoles(Set<String> strings);

	public void addRoles(Set<String> strings);
	
	public void removeRoles(Set<String> strings);

	void setCountry(String country);

	String getPhone();

	void setPhone(String phone);

	public void setCreationDate(Date creationDate);

	public Date getCreationDate();

	public void setModificationDate(Date modificationDate);

	public Date getModificationDate();
	
	public void setValue(String field, String value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	public String[] getAllLabels();

	public String[] getAllValues();
	
	public String getAvatarURL();
	
	public void setAvatarURL(String avatarURL);
	
	public String getUserFolder();
	
	public InternetAddress getInternetAddress();
	
	public String encryptPassword(String pwd);
	
	public String getSite();
	
	public String[] getTaxonomy();
	
	public void setTaxonomy(String[] taxonomy);
	
	public void setSite(String site);

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
	
	public String getCountry();
	
	public String getOrganization();

}
