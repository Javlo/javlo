/*
 * Created on 20-fevr.-2004
 */
package org.javlo.user;

import org.apache.commons.lang3.StringUtils;
import org.javlo.context.ContentContext;
import org.javlo.helper.LocalLogger;
import org.javlo.helper.SecurityHelper;
import org.javlo.helper.StringHelper;

import jakarta.mail.internet.InternetAddress;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.*;

/**
 * @author pvandermaesen
 */
public class UserInfo implements Comparable<IUserInfo>, IUserInfo, Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final String EXTERNAL_LOGIN = "__EXLOGIN__";

	public UserInfo() {
	};

	public UserInfo(String login, String password) {
		this.login = login;
		this.password = password;
	};

	protected String login = "";
	protected String encryptLogin = null;
	protected String password;
	protected String title = "";
	protected String url = "";
	protected String firstName = "";
	protected String lastName = "";
	protected String gender = "";
	protected String email = "";
	protected String organization = "";
	protected String vat = "";
	protected String department = "";
	protected String function = "";
	protected String specialFunction = "";
	protected String experience = "";
	protected String recommendation = "";
	protected String parent = "";
	protected String address = "";
	protected String postCode = "";
	protected String city = "";
	protected String country = "";
	protected String region = "";
	protected String phone = "";
	protected String mobile = "";
	protected String info = "";
	protected String token = "";
	protected String accountType = "default";
	protected String avatarURL = null;
	protected String birthdate = null;
	protected String memberdate = null;
	protected String site = null;
	protected String idNumber;
	protected String nationalRegister;
	protected String nickname;
	protected String health;
	protected String food;
	protected String[] preferredLanguage = new String[0];
	protected Set<String> roles = new HashSet<String>();
	protected Date creationDate = new Date();
	protected Date modificationDate = new Date();
	protected Map<String,String> data = null;
	protected String[] taxonomy;

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getNationalRegister() {
		return nationalRegister;
	}

	@Override
	public void setNationalRegister(String nationalRegister) {
		this.nationalRegister = nationalRegister;
	}

	/**
	 * @return
	 */
	@Override
	public String getEmail() {
		return email;
	}

	/**
	 * @return
	 */
	@Override
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @return
	 */
	@Override
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param string
	 */
	@Override
	public void setEmail(String string) {
		email = string;
	}

	/**
	 * @param string
	 */
	@Override
	public void setFirstName(String string) {
		firstName = string;
	}

	/**
	 * @param string
	 */
	@Override
	public void setLastName(String string) {
		lastName = string;
	}

	/**
	 * @return
	 */
	@Override
	public String getLogin() {
		return login;
	}

	/**
	 * @return
	 */
	@Override
	public String getPassword() {
		return password;
	}

	/**
	 * @param String
	 */
	@Override
	public void setLogin(String login) {
		this.login = login;
		encryptLogin=StringHelper.createFileName(login);
	}
	
	@Override
	public String encryptPassword(String pwd) {	
		return SecurityHelper.encryptPassword(pwd);
	}

	/**
	 * @param String
	 */
	@Override
	public void setPassword(String inPwd) {
		password = inPwd;
	}
	

	@Override
	public String[] getPreferredLanguage() {
		return preferredLanguage;
	}

	public String getPreferredLanguageRaw() {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for (String lang : getPreferredLanguage()) {
			sb.append(sep);
			sb.append(lang);
			sep = PREFERRED_LANGUAGE_SEPARATOR;
		}
		return sb.toString();
	}

	@Override
	public void setPreferredLanguage(String[] preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
	}

	public void setPreferredLanguageRaw(String preferredLanguageRaw) {
		if (preferredLanguageRaw != null && preferredLanguageRaw.trim().length() > 0) {
			this.preferredLanguage = preferredLanguageRaw.split(PREFERRED_LANGUAGE_SEPARATOR);
		}
	}

	/**
	 * @return
	 */
	@Override
	public Set<String> getRoles() {
		return roles;
	}

	public String getRolesRaw() {

		Collection<String> roles = getRoles();

		StringBuffer res = new StringBuffer();
		String sep = "";
		for (String role : roles) {
			res.append(sep);
			res.append(role);
			sep = "" + ROLES_SEPARATOR;
		}
		return res.toString();
	}

	public Set<String> getRolesSet() {
		Set<String> outRoles = new HashSet<String>();
		for (String role : roles) {
			outRoles.add(role);
		}
		return outRoles;
	}

	/**
	 * @param strings
	 */
	@Override
	public synchronized void setRoles(Set<String> inRoles) {
		roles = new HashSet<String>();
		for (String role : inRoles) {
			roles.add(role.trim());
		}		
	}

	@Override
	public void addRoles(Set<String> strings) {
		if (roles == null) {
			roles = new HashSet<String>();
		}
		Set<String> newRoles = new HashSet<String>();
		synchronized (roles) {
			Set<String> rolesList = new HashSet<String>();
			rolesList.addAll(roles);
			rolesList.addAll(strings);
			newRoles.addAll(StringHelper.trimList(rolesList));
		}
		roles = newRoles;
	}

	public void setRolesRaw(String rolesRaw) {
		if (rolesRaw != null) {
			if (rolesRaw.trim().length() > 0) {
				roles = new HashSet<String>(StringHelper.stringToCollectionTrim(rolesRaw, "" + ROLES_SEPARATOR));
			}
		}
	}
	
	@Override
	public void setValue(String field, String value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method[] methods = this.getClass().getMethods();
		for (Method method : methods) {
			if (method.getName().equals("set"+StringUtils.capitalize(field))) {
				if (method.getParameterCount() == 1 && method.getParameters()[0].getType().equals(String.class)) {
					method.invoke(this, value);
				}
			}
		}
	}

	@Override
	public String[] getAllLabels() {
		Collection<String> labels = new LinkedList<String>();
		Method[] methods = this.getClass().getMethods();

		boolean rolesFound = false;
		boolean preferredLanguageFound = false;

		for (Method method : methods) {
			if (method.getName().startsWith("get")) {
				if (method.getReturnType().equals(String.class) || method.getReturnType().equals(Date.class) || method.getReturnType().equals(Map.class)) {
					String name = method.getName().substring(3);
					if (name.equals("RolesRaw")) {
						rolesFound = true;
					}
					if (name.equals("PreferredLanguageRaw")) {
						preferredLanguageFound = true;
					}
					name = StringHelper.firstLetterLower(name);
					labels.add(name);
				}
			}
		}
		if (!rolesFound) {
			labels.add("rolesRaw");
		}
		if (!preferredLanguageFound) {
			labels.add("preferredLanguageRaw");
		}
		String[] res = new String[labels.size()];
		labels.toArray(res);
		Arrays.sort(res);
		return res;
	}

	@Override
	public String[] getAllValues() {
		String[] fields = getAllLabels();
		String[] res = new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			String methodName = "get" + StringHelper.firstLetterUpper(fields[i]);
			try {
				Method method = this.getClass().getMethod(methodName, (Class[]) null);
				if (method.getReturnType().equals(String.class)) {
					String value = (String) method.invoke(this, (Object[]) null);
					res[i] = value;
				} else if (method.getReturnType().equals(Date.class)) {
					String value = StringHelper.renderTime((Date) method.invoke(this, (Object[]) null));
					res[i] = value;
				} else if (method.getReturnType().equals(Map.class)) {
					Map map = (Map)method.invoke(this, (Object[]) null);
					if (map != null) {
						String value = StringHelper.mapToString(map);
						res[i] = value;
					}
				}
			} catch (Exception e) {
				LocalLogger.log(e);
			}
		}
		return res;
	}

	public void setAllValues(Map<String, String> values) {
		String[] labels = getAllLabels();
		String[] arrayValues = new String[getAllLabels().length];
		for (int i = 0; i < labels.length; i++) {
			arrayValues[i] = values.get(labels[i]);
		}
		setAllValues(arrayValues);
	}

	private void setAllValues(String[] values) {
		String[] fields = getAllLabels();
		for (int i = 0; i < fields.length; i++) {
			String methodName = "set" + StringHelper.firstLetterUpper(fields[i]);
			try {
				Method method = this.getClass().getMethod(methodName, new Class[] { String.class });
				method.invoke(this, new Object[] { values[i] });
			} catch (Exception e) {
				try {
					Method method = this.getClass().getMethod(methodName, new Class[] { Date.class });
					method.invoke(this, new Object[] { StringHelper.parseTime(values[i]) });
				} catch (Exception e2) {
					try {
						Method method = this.getClass().getMethod(methodName, new Class[] { Map.class });
						method.invoke(this, new Object[] { StringHelper.stringToMap(values[i]) });
					} catch (Exception e1) {
						e1.printStackTrace();
					}					
					LocalLogger.log(e2);
				}
			}
		}
	}

	/**
	 * compare on last name
	 */
	@Override
	public int compareTo(IUserInfo other) {
		if ((this.getLastName() == null) || (other.getLastName() == null)) {
			return 0;
		}
		return this.getLastName().toLowerCase().compareTo(other.getLastName().toLowerCase());
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	@Override
	public String getAddress() {
		return address;
	}

	@Override
	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String getCity() {
		return city;
	}

	@Override
	public void setCity(String city) {
		this.city = city;
	}

	@Override
	public String getPostCode() {
		return postCode;
	}

	@Override
	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	@Override
	public void setId(String id) {
		setLogin(id);
	}

	@Override
	public String getInfo() {
		return info;
	}

	@Override
	public void setInfo(String info) {
		this.info = info;
	}

	@Override
	public String getCountry() {
		return country;
	}

	@Override
	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public String getPhone() {
		return phone;
	}

	@Override
	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setCreationDate(String dateStr) {
		if (dateStr != null && dateStr.trim().length() > 0) {
			try {
				this.creationDate = StringHelper.parseTime(dateStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}
	
	public String getCreationDateLabel() {
		return StringHelper.renderTime(creationDate);
	}

	public void setModificationDate(String dateStr) {
		try {
			this.modificationDate = StringHelper.parseTime(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	@Override
	public Date getModificationDate() {
		return modificationDate;
	}
	
	public String getBirthdate() {
		return birthdate;
	}

	@Override
	public void setBirthdate(String date) {
		birthdate = date;
	}

	@Override
	public void removeRoles(Set<String> strings) {
		if (roles == null) {
			roles = new HashSet<String>();
		}
		Set<String> newRoles;
		synchronized (roles) {
			Set<String> rolesList = new HashSet<String>();
			rolesList.addAll(roles);
			rolesList.removeAll(strings);
			newRoles = new HashSet<String>(rolesList);
		}
		roles = newRoles;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	@Override
	public String getToken() {
		return token;
	}

	@Override
	public void setToken(String token) {
		this.token = token;
	}
	
	@Override
	public String getTokenCreateIfNotExist() {
		String token = getToken();
		if (StringHelper.isEmpty(token)) {
			resetToken();
		}
		return token;
	}
	
	@Override
	public void resetToken() {
		try {
			token = URLEncoder.encode(StringHelper.getRandomIdBase64() + StringHelper.getRandomString(12), ContentContext.CHARACTER_ENCODING);
			setToken(token);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * return the encrypt login the encrypt login can change when we restart. 
	 */
	@Override
	public String getEncryptLogin() {
		return encryptLogin;
	}
	
	@Override
	public String getAccountType() {	
		return accountType;
	}
	
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	
	@Override
	public void setExternalLoginUser() {
		setPassword(EXTERNAL_LOGIN);
	}
	
	@Override
	public boolean isExternalLoginUser() {
		return getPassword() != null && getPassword().equals(EXTERNAL_LOGIN);
	}

	public String getVat() {
		return vat;
	}

	public void setVat(String vat) {
		this.vat = vat;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAvatarURL() {
		return avatarURL;
	}

	public void setAvatarURL(String avatarURL) {
		this.avatarURL = avatarURL;
	}

	public String getExperience() {
		return experience;
	}

	public void setExperience(String experience) {
		this.experience = experience;
	}

	public String getRecommendation() {
		return recommendation;
	}

	public void setRecommendation(String recommendation) {
		this.recommendation = recommendation;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getSpecialFunction() {
		return specialFunction;
	}

	public void setSpecialFunction(String specialFunction) {
		this.specialFunction = specialFunction;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	@Override
	public String getUserFolder() {
		return StringHelper.createFileName(getLogin());
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getMemberdate() {
		return memberdate;
	}

	public void setMemberdate(String memberdate) {
		this.memberdate = memberdate;
	}

	@Override
	public InternetAddress getInternetAddress() {
		if (getEmail() == null) {
			return null;
		} else {
			try {
				return new InternetAddress(getEmail(), StringHelper.neverNull(getFirstName()) + " " + StringHelper.neverNull(getLastName()));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}
	
	public String getIdNumber() {
		return idNumber;
	}
	
	public void setIdNumber(String idNumber) {
		this.idNumber = idNumber;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public String getHealth() {
		return health;
	}
	
	public void setHealth(String health) {
		this.health = health;
	}
	
	public String getFood() {
		return food;
	}
	
	public void setFood(String food) {
		this.food = food;
	}

	public String getTaxonomyRaw() {
		if (getTaxonomy() == null) {
			return null;
		}
		StringBuffer res = new StringBuffer();
		String sep = "";
		for (String tx : getTaxonomy()) {
			res.append(sep);
			res.append(tx);
			sep = "" + ROLES_SEPARATOR;
		}
		return res.toString();
	}

	public void setTaxonomyRaw(String taxonomyRaw) {
		if (taxonomyRaw != null) {
			if (taxonomyRaw.trim().length() > 0) {
				taxonomy = StringHelper.split(taxonomyRaw, ""+ROLES_SEPARATOR);
			}
		}
	}
	
	@Override
	public String[] getTaxonomy() {
		return taxonomy;
	}
	
	@Override
	public void setTaxonomy(String[] taxonomy) {
		this.taxonomy = taxonomy;
	}


}