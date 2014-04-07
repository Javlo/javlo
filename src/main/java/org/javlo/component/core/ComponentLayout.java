package org.javlo.component.core;

/**
 * represent a simple layout.
 * center, left, right, justify, bold, italic...
 * @author pvandermaesen
 *
 */
public class ComponentLayout {

	private String layout = "";

	public ComponentLayout(String layout) {
		this.layout = layout;
	}
	
	public String getLayout() {
		return layout;
	}

	public boolean isLeft() {
		return layout.contains("l");
	}

	private void addChar(char c) {
		if (layout.indexOf(c) < 0) {
			layout = layout + c;
		}
	}

	private void removeChar(char c) {
		if (layout.indexOf(c) >= 0) {
			layout = layout.replace("" + c, "");
		}
	}

	private void setValue(boolean value, char c) {
		if (value) {
			addChar(c);
		} else {
			removeChar(c);
		}
	}

	public void setLeft(boolean left) {
		setValue(left, 'l');
	}

	public boolean isRight() {
		return layout.indexOf('r') >= 0;
	}

	public void setRight(boolean right) {
		setValue(right, 'r');
	}

	public boolean isCenter() {
		return layout.indexOf('c') >= 0;
	}

	public void setCenter(boolean center) {
		setValue(center, 'c');
	}

	public boolean isJustify() {
		return layout.indexOf('j') >= 0;
	}

	public void setJustify(boolean justify) {
		setValue(justify, 'j');
	}

	public boolean isBold() {
		return layout.contains("b");
	}

	public void setBold(boolean bold) {
		setValue(bold, 'b');
	}

	public boolean isItalic() {
		return layout.contains("i");
	}

	public void setItalic(boolean italic) {
		setValue(italic, 'i');
	}

	public boolean isUnderline() {
		return layout.contains("u");
	}

	public void setUnderline(boolean underline) {
		setValue(underline, 'u');
	}

	public boolean isLineThrough() {
		return layout.indexOf('t') >= 0;
	}

	public void setLineThrough(boolean lineTrough) {
		setValue(lineTrough, 't');
	}

	/**
	 * get the layout as css style.
	 * @return
	 */
	public String getStyle() {
		StringBuffer outStyle = new StringBuffer();
		if (isLeft()) {
			outStyle.append("text-align: left; ");
		} else if (isRight()) {
			outStyle.append("text-align: right; ");
		} else if (isCenter()) {
			outStyle.append("text-align: center; ");
		} else if (isJustify()) {
			outStyle.append("text-align: justify; ");
		}
		if (isBold()) {
			outStyle.append("font-weight: bold; ");
		}
		if (isItalic()) {
			outStyle.append("font-style: italic; ");
		}
		if (isUnderline()) {
			outStyle.append("text-decoration:underline; ");
		}
		if (isLineThrough()) {
			outStyle.append("text-decoration:line-through; ");
		}
		return outStyle.toString();
	}

}
