package org.javlo.text;

import org.javlo.helper.StringHelper;

public class PrefixedText {
	
	private String prefix;
	private String text;
	private String suffix;

	public PrefixedText(String prefix, String text, String suffix) {
		super();
		this.prefix = prefix;
		this.text = text;
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

	@Override
	public String toString() {
		return StringHelper.neverNull(prefix)+text+ StringHelper.neverNull(suffix);
	}
	
}
