package org.javlo.text;

import org.javlo.helper.StringHelper;

public class PrefixedText {

	private String prefix;
	private String text;
	private String suffix;
	private int depth = 0;

	public PrefixedText(String prefix, String text, String suffix) {
		super();
		this.prefix = prefix;
		this.text = text;
		this.suffix = suffix;
	}

	public PrefixedText(String prefix, String text, String suffix, char depthChar) {
		super();		
		if (prefix == null || prefix.trim().length() == 0) {
			text = text.trim();
			while (text.startsWith("" + depthChar)) {
				depth++;
				text = text.substring(1).trim();
			}
		} else {
			prefix = prefix.trim();
			while (prefix.startsWith("" + depthChar)) {
				depth++;
				prefix = prefix.substring(1).trim();
			}
		}
		this.text = text;
		this.prefix = prefix;
		this.suffix = suffix;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public int getDepth() {
		return depth;
	}

	@Override
	public String toString() {
		return StringHelper.neverNull(prefix) + text + StringHelper.neverNull(suffix);
	}

}
