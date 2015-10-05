package org.javlo.user;

import java.util.Collection;
import java.util.LinkedList;

import org.javlo.helper.StringHelper;

public class RoleWrapper {

	private Collection<Role> roles = new LinkedList<Role>();

	public RoleWrapper(Role... roles) {
		for (Role role : roles) {
			addRole(role);
		}
	}

	public void addRole(Role role) {
		roles.add(role);
	}

	public String getMailingSenders() {
		String sep = "";
		String outSenders = "";
		for (Role role : roles) {
			if (role.getMailingSenders() != null && role.getMailingSenders().length() > 0) {
				outSenders = outSenders.concat(sep).concat(role.getMailingSenders());
				sep = ",";
			}
		}
		return outSenders;
	}
	
	/**
	 * accept or not a template, return null if inherited
	 * @param template
	 * @return
	 */
	public Boolean acceptTemplate(String template) {
		Boolean accept = null;
		for (Role role : roles) {			
			if (StringHelper.listContainsItem(role.getTemplateIncluded(), ",", template)) {
				return true;
			} else if (accept == null && StringHelper.listContainsItem(role.getTemplateExcluded(), ",", template)) {
				accept = false;
			}
		}
		return accept;
	}

}
