/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.util.Arrays;
import java.util.List;

import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.exception.RessourceNotFoundException;
import org.javlo.helper.NavigationHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;


/**
 * @author pvandermaesen
 */
public class RSSRegistration extends AbstractPropertiesComponent {

	public static final String TYPE = "rss-registration";

	public static final String CHANNEL_KEY = "channel";
	public static final String HIDE_INVISIBLE_KEY = "hide-invisible";

	public String getChannel() {
		String channel = getFieldValue(CHANNEL_KEY);
		
		// to retrieve old version value
		if (channel == null || channel.length() == 0) {
			String value = getValue();
			if (value != null && value.length() > 0 && !value.contains("#")) {
				channel = value;
				
				setFieldValue(CHANNEL_KEY, channel);
				storeProperties();
				setModify();
			}
		}
		return channel;
	}
	
	public boolean isHideInvisible() {
		return Boolean.valueOf(getFieldValue(HIDE_INVISIBLE_KEY));
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}

	@Override
	protected void init() throws RessourceNotFoundException {
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());

		finalCode.append("<label for=\"" + getChannelName() + "\">");
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		finalCode.append(i18nAccess.getText("component.rss.channel")+" : ");
		finalCode.append("</label>");
		finalCode.append("<input id=\"" + getChannelName() + "\" name=\"" + getChannelName() + "\" value=\"");
		finalCode.append(getChannel());
		finalCode.append("\" />");

		finalCode.append("&nbsp;&nbsp;&nbsp;<label for=\"" + getChannelsName() + "\">");
		finalCode.append(i18nAccess.getText("component.rss.channel-available")+" : ");
		finalCode.append("</label>");
		ContentService content = ContentService.createContent(ctx.getRequest());
		List<String> channelsList = NavigationHelper.getAllRSSChannels(ctx, content.getNavigation(ctx));
		String[] channels = new String[channelsList.size()];
		channelsList.toArray(channels);
		finalCode.append(XHTMLHelper.getInputOneSelect(getChannelsName(), channels, channels, getChannel(), "$('"+getChannelName()+"').setProperty('value',$('"+getChannelsName()+"').getProperty('value'));"));

		finalCode.append("&nbsp;&nbsp;&nbsp;<label for=\"" + getHideInvisibleName() + "\">");
		finalCode.append(i18nAccess.getText("component.rss.hide-invisible")+" : ");
		finalCode.append("</label>");
		finalCode.append("<input type=\"checkbox\" id=\"" + getHideInvisibleName() + "\" name=\"" + getHideInvisibleName() + "\" value=\"true\"");
		if (isHideInvisible()) {
			finalCode.append(" checked=\"checked\"");
		}
		finalCode.append("\" />");

		return finalCode.toString();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	/**
	 * get the XHTML input field name for the channels
	 *
	 * @return a XHTML input field name.
	 */
	public String getChannelName() {
		return "__" + getId() + ID_SEPARATOR + CHANNEL_KEY;
	}

	public String getChannelsName() {
		return "__" + getId() + ID_SEPARATOR + "channels";
	}

	public String getHideInvisibleName() {
		return "__" + getId() + ID_SEPARATOR + HIDE_INVISIBLE_KEY;
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	@Override
	public int getComplexityLevel() {
		return COMPLEXITY_STANDARD;
	}

	@Override
	public void performEdit(ContentContext ctx) {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		String channel = requestService.getParameter(getChannelName(), "");
		String hideInvisibleStr = requestService.getParameter(getHideInvisibleName(), "false");
		boolean hideInvisible = Boolean.valueOf(hideInvisibleStr);

		if (channel != null) {
			String oldChannel = properties.getProperty(CHANNEL_KEY, "");
			if ((!oldChannel.equals(channel)) || (isHideInvisible() ^ hideInvisible)) {
				setFieldValue(CHANNEL_KEY, channel);
				setFieldValue(HIDE_INVISIBLE_KEY, Boolean.toString(hideInvisible));
				storeProperties();
				setModify();
			}
		}
	}

	// useless, actually, or maybe for a call to super for raw data...	
	static final List<String> FIELDS = Arrays.asList(new String[] { CHANNEL_KEY, HIDE_INVISIBLE_KEY });
	
	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}


	@Override
	public String getHeader() {
		return "rss-config";
	}
}
