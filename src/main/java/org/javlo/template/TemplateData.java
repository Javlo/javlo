package org.javlo.template;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.javlo.css.CssColor;
import org.javlo.helper.StringHelper;

public class TemplateData {
	
	public static final int COLOR_LIST_SIZE = 6;
	
	public static final TemplateData EMPTY = new TemplateData();
	private CssColor background = null;
	private CssColor componentBackground = null;
	private CssColor foreground = null;
	private CssColor text = null;
	private CssColor title = null;
	private CssColor special = null;
	private CssColor backgroundMenu = null;
	private CssColor backgroundActive = null;
	private CssColor textMenu = null;
	private CssColor border = null;
	private CssColor link = null;
	private CssColor messagePrimary = null;
	private CssColor messageSecondary = null;
	private CssColor messageSuccess = null;
	private CssColor messageDanger = null;
	private CssColor messageWarning = null;
	private CssColor messageInfo = null;
	private CssColor[] colorList = new CssColor[COLOR_LIST_SIZE];
	private String toolsServer = null;
	private String logo = null;
	private String fontText = null;
	private String fontHeading = null;
	private boolean loginMenu = false;
	private boolean fixMenu = false;
	private boolean largeMenu = false;
	private boolean searchMenu = false;
	private boolean jssearchMenu = false;
	private boolean dropdownMenu = false;
	private boolean fixSidebar = false;
	private boolean small;
	private boolean large;
	private boolean menuLeft = false;
	private boolean menuCenter = false;
	private boolean menuRight = false;
	private boolean menuAround = false;

	public TemplateData() {
	};

	@Deprecated
	private void oldDataLoader(String rawData) {
		String[] data = rawData.split(";");
		try {
			if (data.length > 7) {
				int i = 0;
				if (data[i].length() > 0) {
					setBackground(Color.decode('#' + data[i]));
				}
				i++;
				if (data[i].length() > 0) {
					setForeground(Color.decode('#' + data[i]));
				}
				i++;
				if (data[i].length() > 0) {
					setText(Color.decode('#' + data[i]));
				}
				i++;
				if (data[i].length() > 0) {
					setBackgroundMenu(Color.decode('#' + data[i]));
				}
				i++;
				if (data[i].length() > 0) {
					setTextMenu(Color.decode('#' + data[i]));
				}
				i++;
				if (data[i].length() > 0) {
					setBorder(Color.decode('#' + data[i]));
				}
				i++;
				if (data[i].length() > 0) {
					setLink(Color.decode('#' + data[i]));
				}
				i++;
				if (data[i].length() > 0) {
					setTitle(Color.decode('#' + data[i]));
				}
				i++;
				if (data[i].length() > 0) {
					setSpecial(Color.decode('#' + data[i]));
				}
				i++;
				if (data[i].length() > 0) {
					setToolsServer(data[i]);
				}
				i++;
				if (data[i].length() > 0) {
					setLogo(data[i]);
				}
				i++;
				if (data.length > i && data[i].length() > 0) {
					setBackgroundActive(Color.decode('#' + data[i]));
				}
				i++;
				if (data.length > i && data[i].length() > 0) {
					setFontText(data[i]);
				}
				/** message **/
				if (data.length > 8) {
					i++;
					if (data.length > i && data[i].length() > 0) {
						setMessagePrimary(Color.decode('#' + data[i]));
					}
					i++;
					if (data.length > i && data[i].length() > 0) {
						setMessageSecondary(Color.decode('#' + data[i]));
					}
					i++;
					if (data.length > i && data[i].length() > 0) {
						setMessageSuccess(Color.decode('#' + data[i]));
					}
					i++;
					if (data.length > i && data[i].length() > 0) {
						setMessageDanger(Color.decode('#' + data[i]));
					}
					i++;
					if (data.length > i && data[i].length() > 0) {
						setMessageWarning(Color.decode('#' + data[i]));
					}
					i++;
					if (data.length > i && data[i].length() > 0) {
						setMessageInfo(Color.decode('#' + data[i]));
					}
					i++;
					if (data.length > i && data[i].length() > 0) {
						String[] colors = data[i].split(",");
						int pos = 0;
						for (String c : colors) {
							colorList[pos] = CssColor.getInstance(Color.decode('#' + c));
							pos++;
						}
					}
					i++;
					if (data.length > i && data[i].length() > 0) {
						setComponentBackground(Color.decode('#' + data[i]));
					}
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	private static Color decodeColor(String color) {
		if (color != null && color.length() > 3 && color.startsWith("#") && !color.equals("#null")) {
			return Color.decode(color);
		} else {
			return null;
		}
	}

	public TemplateData(String rawData) {
		if (!rawData.startsWith("map:")) {
			oldDataLoader(rawData);
		} else {
			Map<String, String> map = StringHelper.stringToMap(rawData.substring(4));
			setBackground(decodeColor('#' + map.get("background")));
			setForeground(decodeColor('#' + map.get("foreground")));
			setText(decodeColor('#' + map.get("text")));
			setBackgroundMenu(decodeColor('#' + map.get("backgroundMenu")));
			setTextMenu(decodeColor('#' + map.get("textMenu")));
			setBorder(decodeColor('#' + map.get("border")));
			setLink(decodeColor('#' + map.get("link")));
			setTitle(decodeColor('#' + map.get("title")));
			setSpecial(decodeColor('#' + map.get("special")));
			setToolsServer(map.get("toolsServer"));
			setLogo(map.get("logo"));
			setBackgroundActive(decodeColor('#' + map.get("backgroundActive")));
			setComponentBackground(decodeColor('#' + map.get("componentBackground")));
			setFontText(map.get("fontText"));
			setFontHeading(map.get("fontHeading"));
			setMessagePrimary(decodeColor('#' + map.get("messagePrimary")));
			setMessageSecondary(decodeColor('#' + map.get("messageSecondary")));
			setMessageSuccess(decodeColor('#' + map.get("messageSuccess")));
			setMessageDanger(decodeColor('#' + map.get("messageDanger")));
			setMessageWarning(decodeColor('#' + map.get("messageWarning")));
			setMessageInfo(decodeColor('#' + map.get("messageInfo")));
			if (map.get("colors") != null) {
				String[] colors = map.get("colors").split(",");
				int pos = 0;
				for (String c : colors) {
					colorList[pos] = CssColor.getInstance(decodeColor('#' + c));
					pos++;
				}
			}
			/* layout */
			setFixMenu(StringHelper.isTrue(map.get("fixMenu")));
			setLargeMenu(StringHelper.isTrue(map.get("largeMenu")));
			setLoginMenu(StringHelper.isTrue(map.get("loginMenu")));
			setSearchMenu(StringHelper.isTrue(map.get("search")));
			setJssearchMenu(StringHelper.isTrue(map.get("jssearch")));
			setDropdownMenu(StringHelper.isTrue(map.get("dropdown")));
			setLarge(StringHelper.isTrue(map.get("large")));
			setSmall(StringHelper.isTrue(map.get("small")));
			setFixSidebar(StringHelper.isTrue(map.get("fixSidebar")));
		}
	}
	
	@Override
	public String toString() {
		Map<String,String> outData = new HashMap<String,String>();
		
		outData.put("background", StringHelper.colorToHexStringNotNull(getBackground()));
		outData.put("foreground", StringHelper.colorToHexStringNotNull(getForeground()));
		outData.put("text", StringHelper.colorToHexStringNotNull(getText()));
		outData.put("backgroundMenu", StringHelper.colorToHexStringNotNull(getBackgroundMenu()));
		outData.put("textMenu", StringHelper.colorToHexStringNotNull(getTextMenu()));
		outData.put("border", StringHelper.colorToHexStringNotNull(getBorder()));
		outData.put("link", StringHelper.colorToHexStringNotNull(getLink()));
		outData.put("title", StringHelper.colorToHexStringNotNull(getTitle()));
		outData.put("special", StringHelper.colorToHexStringNotNull(getSpecial()));
		outData.put("toolsServer", getToolsServer());
		outData.put("logo", getLogo());
		outData.put("backgroundActive", StringHelper.colorToHexStringNotNull(getBackgroundActive()));
		outData.put("componentBackground", StringHelper.colorToHexStringNotNull(getComponentBackground()));
		outData.put("messagePrimary", StringHelper.colorToHexStringNotNull(getMessagePrimary()));
		outData.put("messageSecondary", StringHelper.colorToHexStringNotNull(getMessageSecondary()));
		outData.put("messageSuccess", StringHelper.colorToHexStringNotNull(getMessageSuccess()));
		outData.put("messageDanger", StringHelper.colorToHexStringNotNull(getMessageDanger()));
		outData.put("messageWarning", StringHelper.colorToHexStringNotNull(getMessageWarning()));
		outData.put("messageInfo", StringHelper.colorToHexStringNotNull(getMessageInfo()));
		outData.put("fontText", getFontText());
		outData.put("fontHeading", getFontHeading());
		outData.put("fixMenu", ""+isFixMenu());
		outData.put("loginMenu", ""+isLoginMenu());
		outData.put("largeMenu", ""+isLargeMenu());
		outData.put("large", ""+isLarge());
		outData.put("small", ""+isSmall());
		outData.put("jssearch", ""+isJssearchMenu());
		outData.put("search", ""+isSearchMenu());
		outData.put("fixSidebar", ""+isFixSidebar());
		outData.put("menuLeft", ""+isMenuLeft());
		outData.put("menuRight", ""+isMenuRight());
		outData.put("menuCenter", ""+isMenuCenter());
		outData.put("menuAround", ""+isMenuAround());
		
		String colors = "";
		String sep = "";
		for (int i = 0; i < TemplateData.COLOR_LIST_SIZE; i++) {
			colors+=sep;
			colors+=StringHelper.colorToHexStringNotNull(getColorList()[i]);
			sep = ",";
		}
		outData.put("colors", colors);
		outData.put("font", getFontText());
		
		return "map:"+StringHelper.mapToString(outData);
//		StringBuffer out = new StringBuffer();
//		out.append(StringHelper.colorToHexStringNotNull(getBackground()));
//		out.append(';');
//		out.append(StringHelper.colorToHexStringNotNull(getForeground()));
//		out.append(';');
//		out.append(StringHelper.colorToHexStringNotNull(getText()));
//		out.append(';');
//		out.append(StringHelper.colorToHexStringNotNull(getBackgroundMenu()));
//		out.append(';');
//		out.append(StringHelper.colorToHexStringNotNull(getTextMenu()));
//		out.append(';');
//		out.append(StringHelper.colorToHexStringNotNull(getBorder()));
//		out.append(';');
//		out.append(StringHelper.colorToHexStringNotNull(getLink()));
//		out.append(';');
//		out.append(StringHelper.colorToHexStringNotNull(getTitle()));
//		out.append(';');
//		out.append(StringHelper.colorToHexStringNotNull(getSpecial()));
//		out.append(';');
//		out.append(getToolsServer());
//		out.append(';');
//		out.append(getLogo());
//		out.append(';');
//		out.append(StringHelper.colorToHexStringNotNull(getBackgroundActive()));
//		out.append(';');
//		out.append(StringHelper.colorToHexStringNotNull(getMessagePrimary()));
//		out.append(';');
//		out.append(StringHelper.colorToHexStringNotNull(getMessageSecondary()));
//		out.append(';');
//		out.append(StringHelper.colorToHexStringNotNull(getMessageSuccess()));
//		out.append(';');
//		out.append(StringHelper.colorToHexStringNotNull(getMessageDanger()));
//		out.append(';');
//		out.append(StringHelper.colorToHexStringNotNull(getMessageWarning()));
//		out.append(';');
//		out.append(StringHelper.colorToHexStringNotNull(getMessageInfo()));
//		out.append(';');
//		out.append(getFont());
//		out.append(';');
//		String sep = "";
//		for (int i = 0; i < 6; i++) {
//			out.append(sep);
//			out.append(StringHelper.colorToHexStringNotNull(getColorList()[i]));
//			sep = ",";
//		}
//		out.append(';');
//		out.append(StringHelper.colorToHexStringNotNull(getComponentBackground()));
//		return out.toString();
	}

	public CssColor[] getColorList() {
		return colorList;
	}

	public void setColorList(Color color, int i) {
		this.colorList[i] = CssColor.getInstance(color);
	}
	
	public boolean isColorListFilled() {
		for (int i = 0; i < colorList.length; i++) {
			if (colorList[i] != null) {
				return true;
			}
		}
		return false;
	}

	public Color getBackground() {
		return background;
	}

	public Color getBackgroundMenu() {
		return backgroundMenu;
	}

	public Color getBackgroundActive() {
		return backgroundActive;
	}

	public Color getBorder() {
		return border;
	}

	public Color getForeground() {
		return foreground;
	}

	public Color getLink() {
		return link;
	}

	public String getLogo() {
		return logo;
	}

	public Color getText() {
		return text;
	}

	public Color getTextMenu() {
		return textMenu;
	}

	public String getToolsServer() {
		return toolsServer;
	}

	public void setBackground(Color background) {
		this.background = CssColor.getInstance(background);
	}

	public void setBackgroundMenu(Color backgroundMenu) {
		this.backgroundMenu = CssColor.getInstance(backgroundMenu);
	}

	public void setBackgroundActive(Color backgroundActive) {
		this.backgroundActive = CssColor.getInstance(backgroundActive);
	}

	public void setBorder(Color border) {
		this.border = CssColor.getInstance(border);
	}

	public void setForeground(Color foreGround) {
		foreground = CssColor.getInstance(foreGround);
	}

	public void setLink(Color link) {
		this.link = CssColor.getInstance(link);
	}

	public void setLogo(String logo) {
		if (logo != null && logo.equalsIgnoreCase("null")) {
			this.logo = null;
		} else {
			this.logo = logo;
		}
	}

	public void setText(Color text) {
		this.text = CssColor.getInstance(text);
	}

	public void setTextMenu(Color textMenu) {
		this.textMenu = CssColor.getInstance(textMenu);
	}

	public void setToolsServer(String toolsServer) {
		this.toolsServer = toolsServer;
	}

	public CssColor getTitle() {
		return title;
	}

	public void setTitle(Color title) {
		this.title = CssColor.getInstance(title);
	}

	public CssColor getSpecial() {
		return special;
	}

	public void setSpecial(Color special) {
		this.special = CssColor.getInstance(special);
	}

	/**
	 * check difference between two template data, some difference don't need deploy, some need deploy
	 * @return
	 */
	public int hashCodeForDeployTemplate() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((background == null) ? 0 : background.hashCode());
		result = prime * result + ((backgroundMenu == null) ? 0 : backgroundMenu.hashCode());
		result = prime * result + ((backgroundActive == null) ? 0 : backgroundActive.hashCode());
		result = prime * result + ((componentBackground == null) ? 0 : componentBackground.hashCode());
		result = prime * result + ((border == null) ? 0 : border.hashCode());
		result = prime * result + ((colorList == null) ? 0 : colorList.hashCode());
		result = prime * result + ((foreground == null) ? 0 : foreground.hashCode());
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((special == null) ? 0 : special.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((textMenu == null) ? 0 : textMenu.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((toolsServer == null) ? 0 : toolsServer.hashCode());
		result = prime * result + ((fontText == null) ? 0 : fontText.hashCode());
		result = prime * result + ((fontHeading == null) ? 0 : fontHeading.hashCode());
		result = prime * result + ((messagePrimary == null) ? 0 : messagePrimary.hashCode());
		result = prime * result + ((messageSecondary == null) ? 0 : messageSecondary.hashCode());
		result = prime * result + ((messageSuccess == null) ? 0 : messageSuccess.hashCode());
		result = prime * result + ((messageDanger == null) ? 0 : messageDanger.hashCode());
		result = prime * result + ((messageWarning == null) ? 0 : messageWarning.hashCode());
		result = prime * result + ((messageInfo == null) ? 0 : messageInfo.hashCode());
		return result;
	}


	public String getFontText() {
		return fontText;
	}

	public void setFontText(String font) {
		this.fontText = font;
	}

	public CssColor getMessagePrimary() {
		return messagePrimary;
	}

	public void setMessagePrimary(Color messagePrimary) {
		this.messagePrimary = CssColor.getInstance(messagePrimary);
	}

	public CssColor getMessageSecondary() {
		return messageSecondary;
	}

	public void setMessageSecondary(Color messageSecondary) {
		this.messageSecondary = CssColor.getInstance(messageSecondary);
	}

	public CssColor getMessageSuccess() {
		return messageSuccess;
	}

	public void setMessageSuccess(Color messageSuccess) {
		this.messageSuccess = CssColor.getInstance(messageSuccess);
	}

	public CssColor getMessageDanger() {
		return messageDanger;
	}

	public void setMessageDanger(Color messageDanger) {
		this.messageDanger = CssColor.getInstance(messageDanger);
	}

	public CssColor getMessageWarning() {
		return messageWarning;
	}

	public void setMessageWarning(Color messageWarning) {
		this.messageWarning = CssColor.getInstance(messageWarning);
	}

	public CssColor getMessageInfo() {
		return messageInfo;
	}

	public void setMessageInfo(Color messageInfo) {
		this.messageInfo = CssColor.getInstance(messageInfo);
	}

	public CssColor getComponentBackground() {
		return componentBackground;
	}

	public void setComponentBackground(Color componentBackground) {
		this.componentBackground = CssColor.getInstance(componentBackground);
	}

	public boolean isFixMenu() {
		return fixMenu;
	}

	public void setFixMenu(boolean fixMenu) {
		this.fixMenu = fixMenu;
	}

	public boolean isLargeMenu() {
		return largeMenu;
	}

	public void setLargeMenu(boolean largeMenu) {
		this.largeMenu = largeMenu;
	}

	public boolean isSearchMenu() {
		return searchMenu;
	}

	public void setSearchMenu(boolean search) {
		this.searchMenu = search;
	}

	public boolean isJssearchMenu() {
		return jssearchMenu;
	}

	public void setJssearchMenu(boolean jssearch) {
		this.jssearchMenu = jssearch;
	}

	public boolean isDropdownMenu() {
		return dropdownMenu;
	}

	public void setDropdownMenu(boolean dropdown) {
		this.dropdownMenu = dropdown;
	}

	public String getFontHeading() {
		return fontHeading;
	}

	public void setFontHeading(String fontHeading) {
		this.fontHeading = fontHeading;
	}

	public boolean isSmall() {
		return small;
	}

	public void setSmall(boolean small) {
		this.small = small;
	}

	public boolean isLarge() {
		return large;
	}

	public void setLarge(boolean large) {
		this.large = large;
	}

	public boolean isLoginMenu() {
		return loginMenu;
	}

	public void setLoginMenu(boolean login) {
		this.loginMenu = login;
	}

	public boolean isFixSidebar() {
		return fixSidebar;
	}

	public void setFixSidebar(boolean fixSidebar) {
		this.fixSidebar = fixSidebar;
	}

	public boolean isMenuLeft() {
		return menuLeft;
	}

	public void setMenuLeft(boolean menuLeft) {
		this.menuLeft = menuLeft;
	}

	public boolean isMenuCenter() {
		return menuCenter;
	}

	public void setMenuCenter(boolean menuCenter) {
		this.menuCenter = menuCenter;
	}

	public boolean isMenuRight() {
		return menuRight;
	}

	public void setMenuRight(boolean menuRight) {
		this.menuRight = menuRight;
	}

	public boolean isMenuAround() {
		return menuAround;
	}

	public void setMenuAround(boolean menuBetween) {
		this.menuAround = menuBetween;
	}
	
	public String getMenuAlign() {
		if (isMenuRight()) {
			return "end";
		} else if (isMenuCenter()) {
			return "center";
		} else if (isMenuAround()) {
			return "around";
		} else {
			return "left";
		}
	}
	
}