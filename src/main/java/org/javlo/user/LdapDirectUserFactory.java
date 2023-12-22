package org.javlo.user;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.javlo.config.StaticConfig;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.user.exception.UserAllreadyExistException;
import org.javlo.utils.TimeMap;

public class LdapDirectUserFactory extends AdminUserFactory {

	private static final String CRYPT_PASSWORD_PREFIX = "{CRYPT}";

	public static Logger logger = Logger.getLogger(LdapDirectUserFactory.class.getName());

	User currentUser;

	TimeMap<String, List<IUserInfo>> userInfoListCache = new TimeMap<String, List<IUserInfo>>(60*60);

	Map<String, IUserInfo> userRoles = new TreeMap<String, IUserInfo>();
	
	private GlobalContext globalContext;

	public LdapDirectUserFactory() {
	}
	
	@Override
	public void addUserInfo(IUserInfo userInfo) throws UserAllreadyExistException {
	}

	@Override
	public User autoLogin(HttpServletRequest request, String login) {
		currentUser = super.autoLogin(request, login);
		return currentUser;
	}

	@Override
	public void clearUserInfoList() {
		userInfoListCache.clear();
	}

	@Override
	public UserInfo createUserInfos() {
		return new UserInfo();
	}

	/*
	 * @Override public User login(GlobalContext globalContext, String login, String
	 * password) { return login((HttpServletRequest) null, login, password); }
	 */

	@Override
	public void deleteUser(String login) {
	}

	
	@Override
	public User getCurrentUser(HttpSession session) {
		return currentUser;
	}

	@Override
	public User getUser(String login) {
		IUserInfo userInfo = getUserInfo(globalContext, login);
		if (userInfo != null) {
			return new User(userInfo);
		} else {
			return null;
		}
	}

	private Map<String, Set<String>> getUserByGroups(GlobalContext globalContext) throws Exception {
		Map<String, Set<String>> outGroup = new HashMap<String, Set<String>>();
		StaticConfig staticConfig = globalContext.getStaticConfig();
		File queriesFile = new File(URLHelper.mergePath(globalContext.getDataFolder(), "ldap_queries.properties"));
		if (!queriesFile.exists()) {
			logger.severe("roles maping for LDAP file not found : " + queriesFile);
			return null;
		}
		Properties props = ResourceHelper.loadProperties(queriesFile);

		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, staticConfig.getLDAPInitalContextFactory());
			env.put(Context.PROVIDER_URL, staticConfig.getLDAPProviderURL());
			env.put(Context.SECURITY_AUTHENTICATION, staticConfig.getLDAPSecurityAuthentification());
			env.put(Context.SECURITY_PRINCIPAL, staticConfig.getLDAPSecurityPrincipal());
			env.put(Context.SECURITY_CREDENTIALS, staticConfig.getLDAPSecurityCredentials());
			DirContext ctx = new InitialDirContext(env);
			SearchResult sr = null;
			Enumeration<Object> iter = props.keys();
			String baseDN = null;
			baseDN = props.getProperty("baseDNGroup");
			if (baseDN == null) {
				throw new Exception("baseDN (group) property not found, must be the first one");
			}
			while (iter.hasMoreElements()) {
				String roles = (String) iter.nextElement();
				NamingEnumeration<SearchResult> ne = ctx.search(baseDN, props.getProperty(roles), null);
				if (ne.hasMore()) {
					try {
						sr = ne.next();
						Attribute att = sr.getAttributes().get("memberUid");
						NamingEnumeration members = att.getAll();
						Set<String> membersList = new HashSet<String>();
						while (members.hasMore()) {
							membersList.add(members.next().toString());
						}
						for (String role : StringHelper.stringToCollection(roles, ",")) {
							outGroup.put(role, membersList);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return outGroup;
	}
	
	private IUserInfo getUserInfo(GlobalContext globalContext, String login) {
		try {
			Map<String, Set<String>> rolesMap = getUserByGroups(globalContext);
			StaticConfig staticConfig = globalContext.getStaticConfig();
			File queriesFile = new File(URLHelper.mergePath(globalContext.getDataFolder(), "ldap_queries.properties"));
			if (!queriesFile.exists()) {
				logger.severe("roles maping for LDAP file not found : " + queriesFile);
				return null;
			}
			Properties props = ResourceHelper.loadProperties(queriesFile);
			try {
				Hashtable<String, String> env = new Hashtable<String, String>();
				env.put(Context.INITIAL_CONTEXT_FACTORY, staticConfig.getLDAPInitalContextFactory());
				env.put(Context.PROVIDER_URL, staticConfig.getLDAPProviderURL());
				env.put(Context.SECURITY_AUTHENTICATION, staticConfig.getLDAPSecurityAuthentification());
				env.put(Context.SECURITY_PRINCIPAL, staticConfig.getLDAPSecurityPrincipal());
				env.put(Context.SECURITY_CREDENTIALS, staticConfig.getLDAPSecurityCredentials());
				DirContext ctx = new InitialDirContext(env);
				SearchResult sr = null;
				String baseDN = null;
				baseDN = props.getProperty("baseDNUser");
				if (baseDN == null) {
					throw new Exception("baseDN property not found, must be the first one");
				}

				NamingEnumeration<SearchResult> ne = ctx.search(baseDN, "(uid="+login+")", null);				
				if (ne.hasMore()) {
					try {
						sr = ne.next();
						login = sr.getAttributes().get("uid").get().toString();

						UserInfo userInfo = createUserInfos();
						if (userInfo == null) {
							userInfo = new UserInfo();
						}
						userInfo.setLogin(login);
						Attribute mailAttr = sr.getAttributes().get("mail");
						if (mailAttr != null) {
							userInfo.setEmail(mailAttr.get().toString());
						}
						userRoles.put(login, userInfo);
						Set<String> roles = new HashSet<String>();
						for (Map.Entry<String, Set<String>> entry : rolesMap.entrySet()) {
							if (entry.getValue().contains(login)) {
								roles.add(entry.getKey());
							}
						}
						userInfo.addRoles(roles);
						return userInfo;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (NamingException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<IUserInfo> getUserInfo(GlobalContext globalContext) {
		List<IUserInfo> users = new LinkedList<IUserInfo>();
		try {
			Map<String, Set<String>> rolesMap = getUserByGroups(globalContext);
			StaticConfig staticConfig = globalContext.getStaticConfig();
			File queriesFile = new File(URLHelper.mergePath(globalContext.getDataFolder(), "ldap_queries.properties"));
			if (!queriesFile.exists()) {
				logger.severe("roles maping for LDAP file not found : " + queriesFile);
				return null;
			}
			Properties props = ResourceHelper.loadProperties(queriesFile);
			try {
				Hashtable<String, String> env = new Hashtable<String, String>();
				env.put(Context.INITIAL_CONTEXT_FACTORY, staticConfig.getLDAPInitalContextFactory());
				env.put(Context.PROVIDER_URL, staticConfig.getLDAPProviderURL());
				env.put(Context.SECURITY_AUTHENTICATION, staticConfig.getLDAPSecurityAuthentification());
				env.put(Context.SECURITY_PRINCIPAL, staticConfig.getLDAPSecurityPrincipal());
				env.put(Context.SECURITY_CREDENTIALS, staticConfig.getLDAPSecurityCredentials());
				DirContext ctx = new InitialDirContext(env);
				SearchResult sr = null;
				String baseDN = null;
				baseDN = props.getProperty("baseDNUser");
				if (baseDN == null) {
					throw new Exception("baseDN property not found, must be the first one");
				}

				NamingEnumeration<SearchResult> ne = ctx.search(baseDN, "(uid=*)", null);				
				while (ne.hasMore()) {
					try {
						sr = ne.next();
						String login = sr.getAttributes().get("uid").get().toString();

						UserInfo userInfo = createUserInfos();
						if (userInfo == null) {
							userInfo = new UserInfo();
						}
						userInfo.setLogin(login);
						Attribute mailAttr = sr.getAttributes().get("mail");
						if (mailAttr != null) {
							userInfo.setEmail(mailAttr.get().toString());
						}
						userRoles.put(login, userInfo);
						Set<String> roles = new HashSet<String>();
						for (Map.Entry<String, Set<String>> entry : rolesMap.entrySet()) {
							if (entry.getValue().contains(login)) {
								roles.add(entry.getKey());
							}
						}
						userInfo.addRoles(roles);
						users.add(userInfo);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (NamingException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return users;
	}

	private IUserInfo getUserInfo(GlobalContext globalContext, HttpSession session, String login, String password) {
		try {
			Map<String, Set<String>> rolesMap = getUserByGroups(globalContext);
			StaticConfig staticConfig = StaticConfig.getInstance(session.getServletContext());
			Properties props = (Properties) session.getServletContext().getAttribute("ldap_queries");
			File queriesFile = new File(URLHelper.mergePath(globalContext.getDataFolder(), "ldap_queries.properties"));
			if (!queriesFile.exists()) {
				logger.severe("roles maping for LDAP file not found : " + queriesFile);
				return null;
			}
			if (props == null) {
				props = ResourceHelper.loadProperties(queriesFile);
				session.getServletContext().setAttribute("ldap_queries", props);
			}

			try {
				Hashtable<String, String> env = new Hashtable<String, String>();
				env.put(Context.INITIAL_CONTEXT_FACTORY, staticConfig.getLDAPInitalContextFactory());
				env.put(Context.PROVIDER_URL, staticConfig.getLDAPProviderURL());
				env.put(Context.SECURITY_AUTHENTICATION, staticConfig.getLDAPSecurityAuthentification());
				env.put(Context.SECURITY_PRINCIPAL, staticConfig.getLDAPSecurityLogin(login));
				env.put(Context.SECURITY_CREDENTIALS, password);
				DirContext ctx = new InitialDirContext(env);
				SearchResult sr = null;
				String attr = null;
				List<String> errorLogins = new ArrayList<String>();
				String baseDN = null;
				baseDN = props.getProperty("baseDNUser");
				if (baseDN == null) {
					throw new Exception("baseDN property not found, must be the first one");
				}

				NamingEnumeration<SearchResult> ne = ctx.search(baseDN, "(uid=" + login + ")", null);
				if (ne.hasMore()) {
					try {
						sr = ne.next();
						login = sr.getAttributes().get(attr = "uid").get().toString();
						if (login == null || login.contains(" ")) {
							logger.warning("unvalid login : " + login);
						} else {
							logger.info("valid login : " + login);
							UserInfo userInfo = createUserInfos();
							if (userInfo == null) {
								userInfo = new UserInfo();
							}
							userInfo.setLogin(login);
							Attribute mailAttr = sr.getAttributes().get(attr = "mail");
							if (mailAttr != null) {
								userInfo.setEmail(mailAttr.get().toString());
							}
							userRoles.put(login, userInfo);
							Set<String> roles = new HashSet<String>();
							for (Map.Entry<String, Set<String>> entry : rolesMap.entrySet()) {
								if (entry.getValue().contains(login)) {
									roles.add(entry.getKey());
								}
							}
							userInfo.addRoles(roles);
							return userInfo;
						}
					} catch (Exception e) {
						logger.severe("error loading user " + login + " on attribute " + attr + ": " + e.getMessage());
						errorLogins.add(login);
					}
				}
				logger.info(userRoles.size() + " users imported");
				logger.info(errorLogins.size() + " errors found");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (NamingException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<IUserInfo> getUserInfoForRoles(String[] inRoles) {
		return getUserInfoList();
	}

	@Override
	public List<IUserInfo> getUserInfoList() {
		if (userInfoListCache.get("userInfoList") == null) {
			userInfoListCache.put("userInfoList", getUserInfo(globalContext));
		}
		return userInfoListCache.get("userInfoList");
	}

	@Override
	public IUserInfo getUserInfos(String id) {
		return null;
	}

	@Override
	public void init(GlobalContext globalContext, HttpSession session) {
		this.globalContext = globalContext;
	}

	@Override
	public boolean isStandardStorage() {
		return false;
	}

	@Override
	public User login(HttpServletRequest request, String login, String password) {
		User user = super.login(request, login, password);
		if (user == null) {
			GlobalContext globalContext = GlobalContext.getInstance(request);
			IUserInfo userInfo = getUserInfo(globalContext, request.getSession(), login, password);
			if (userInfo != null) {
				User outUser = new User(userInfo);
				EditContext editContext = EditContext.getInstance(globalContext, request.getSession());
				editContext.setEditUser(outUser);
				request.getSession().setAttribute(getSessionKey(), outUser);
				/** reload module **/
				try {
					ModulesContext.getInstance(request.getSession(), globalContext).loadModule(request.getSession(), globalContext);
				} catch (ModuleException e) {
					e.printStackTrace();
				}
				if (outUser != null) {
					outUser.setEditor(true);
				}
				return outUser;
			}
		} else {
			currentUser = user;
		}
		return user;
	}

	@Override
	public void mergeUserInfo(IUserInfo userInfo) {
	}

	@Override
	public void releaseUserInfoList() {
		userInfoListCache.clear();
	}

	@Override
	public void store() {
	}

	@Override
	public void updateUserInfo(IUserInfo userInfo) {
	}

}
