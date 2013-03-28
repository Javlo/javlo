package org.javlo.template;

import java.util.Date;

import javax.servlet.http.HttpSession;



public class TemplateSearchContext {

	private static String KEY = TemplateSearchContext.class.getClass().getName();
	
	private String owner = "";

	private Date date = null;

	private String authors = "";

	private String source = "";

	private int depth = 0;

	private String dominantColor = "";

	public static TemplateSearchContext getInstance(HttpSession session) {
		TemplateSearchContext out = (TemplateSearchContext) session.getAttribute(KEY);
		if (out == null) {
			out = new TemplateSearchContext();
			session.setAttribute(KEY, out);
		}
		return out;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public String getDominantColor() {
		return dominantColor;
	}

	public void setDominantColor(String dominanteColor) {
		this.dominantColor = dominanteColor;
	}

	public boolean match(Template template) {
		if (getAuthors().trim().length() > 0) {
			if (!template.getAuthors().equals(getAuthors())) {
				return false;
			}
		}
		if (getOwner().trim().length() > 0) {
			if (!template.getOwner().equals(getOwner())) {
				return false;
			}
		}
		if (getSource().trim().length() > 0) {
			if (!template.getSource().equals(getSource())) {
				return false;
			}
		}
		if (getDominantColor().trim().length() > 0) {
			if (!getDominantColor().trim().equals("none")) {
				if (!template.getDominantColor().equals(getDominantColor())) {
					return false;
				}
			}
		}
		if (getDate() != null) {
			if (template.getCreationDate().getTime() < getDate().getTime()) {
				return false;
			}
		}
		if (getDepth() > 0) {
			if (template.getDepth() < getDepth()) {
				return false;
			}
		}
		return true;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		if (owner == null) {
			owner = "";
		}
		this.owner = owner;
	}

}
