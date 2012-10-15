/*
 * Created on 20-fevr.-2004
 */
package org.javlo.user;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.javlo.helper.Logger;
import org.javlo.helper.StringHelper;

/**
 * @author pvandermaesen
 */
public class UserInfo implements Comparable<IUserInfo>, IUserInfo, Serializable {

	private static final long serialVersionUID = 1L;

	public UserInfo() {
	};

	public UserInfo(String login, String password) {
		this.login = login;
		this.password = password;
	};

	private String login = "";
	private String password;
	private String title = "";
	private String firstName = "";
	private String lastName = "";
	private String email = "";
	private String organization = "";
	private String function = "";
	private String address = "";
	private String postCode = "";
	private String city = "";
	private String country = "";
	private String phone = "";
	private String mobile = "";
	private String info = "";
	private String[] preferredLanguage = new String[0];
	private Set<String> roles = new HashSet<String>();
	private Date creationDate = new Date();
	private Date modificationDate = new Date();

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
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
	 * @param string
	 */
	@Override
	public void setLogin(String string) {
		login = string;
	}

	/**
	 * @param string
	 */
	@Override
	public void setPassword(String string) {
		password = string;
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
	public void setRoles(Set<String> inRoles) {
		roles = inRoles;
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
			newRoles.addAll(rolesList);
		}
		roles = newRoles;
	}

	public void setRolesRaw(String rolesRaw) {
		if (rolesRaw != null) {
			if (rolesRaw.trim().length() > 0) {
				roles = new HashSet<String>(StringHelper.stringToCollection(rolesRaw, "" + ROLES_SEPARATOR));
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
				if (method.getReturnType().equals(String.class) || method.getReturnType().equals(Date.class)) {
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
				}
			} catch (Exception e) {
				Logger.log(e);
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
					Logger.log(e2);
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPostCode() {
		return postCode;
	}

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

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setCreationDate(String dateStr) {
		try {
			this.creationDate = StringHelper.parseTime(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
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
}