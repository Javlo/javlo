/*
 * Creat	ed on 19-sept.-2003 
 */
package org.javlo.component.text;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.XHTMLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class ContactText extends AbstractVisualComponent {

	private static final Object LOCK_UPDATE_CACHE = new Object();

	private String key = "";
	private String content = "";

	@Override
	public String getType() {
		return "contact-text";
	}

	@Override
	protected void init(ComponentBean bean, ContentContext ctx) throws Exception {
		String[] value = bean.getValue().split("=");
		if (value.length == 2) {
			key = value[0];
			content = value[1];
		} else if (value.length == 1) {
			key = value[0];
		}
		super.init(bean, ctx);
	}

	public String[] getChoices(ContentContext ctx) throws Exception {
		synchronized (LOCK_UPDATE_CACHE) {
			Map<String, String> cache = getContactCache(ctx);
			String[] choices = new String[cache.keySet().size() + 1];
			choices[0] = "";
			int i = 1;
			Iterator<String> keys = cache.keySet().iterator();
			while (keys.hasNext()) {
				choices[i] = keys.next();
				i++;
			}
			return choices;
		}
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println(XHTMLHelper.textToXHTMLDIV(getContent(ctx)));
		out.close();
		return new String(outStream.toByteArray());
	}

	public String getInputChoiceName() {
		return "choice_" + getId();
	}

	public String getInputKeyName() {
		return "key_" + getId();
	}

	public String getInputContentName() {
		return "content_" + getId();
	}

	public String getInputAddName() {
		return "add_" + getId();
	}

	public String getContent(ContentContext ctx) {
		synchronized (LOCK_UPDATE_CACHE) {
			if (isContactCache(ctx) && getContactKey().trim().length() > 0) {
				try {
					return getContactCache(ctx).get(getContactKey());
				} catch (Exception e) {
					e.printStackTrace();
					return content;
				}
			} else {
				return content;
			}
		}
	}

	public String getContent() {
		return content;
	}

	public String getContactKey() {
		return key;
	}

	@Override
	public String getValue(ContentContext ctx) {
		try {
			return getContactKey() + '=' + getContent(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	};

	protected boolean isContactCache(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		return globalContext.getAttribute(getContactCacheKey(ctx)) != null;
	}

	protected Map<String, String> getContactCache(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		synchronized (LOCK_UPDATE_CACHE) {
			Map<String, String> contactCache = (Map<String, String>) globalContext.getAttribute(getContactCacheKey(ctx));
			if (contactCache == null) {
				contactCache = new Hashtable<String, String>();
				ContentService content = ContentService.getInstance(ctx.getRequest());
				MenuElement rootPage = content.getNavigation(ctx);
				MenuElement[] children = rootPage.getAllChildren();
				for (MenuElement page : children) {
					List<IContentVisualComponent> comps = page.getContentByType(ctx, getType());
					for (IContentVisualComponent comp : comps) {
						ContactText contactComp = (ContactText) comp;
						if (contactComp.getContactKey().trim().length() > 0) {
							if (contactCache.get(contactComp.getContactKey()) == null) {
								contactCache.put(contactComp.getContactKey(), contactComp.getContent());
							}
						}
					}
				}
				globalContext.setAttribute(getContactCacheKey(ctx), contactCache);
			}
			return contactCache;
		}
	}

	protected static String getContactCacheKey(ContentContext ctx) {
		return ContactText.class.getCanonicalName() + '_' + ctx.getRequestContentLanguage();
	}

	protected void updateAllContent(ContentContext ctx) throws Exception {
		synchronized (LOCK_UPDATE_CACHE) {
			Map<String, String> contactCache = getContactCache(ctx);
			ContentService content = ContentService.getInstance(ctx.getRequest());
			MenuElement rootPage = content.getNavigation(ctx);
			MenuElement[] children = rootPage.getAllChildren();
			for (MenuElement page : children) {
				List<IContentVisualComponent> comps = page.getContentByType(ctx, getType());
				for (IContentVisualComponent comp : comps) {
					ContactText contactComp = (ContactText) comp;
					if (contactComp.getContactKey().trim().length() > 0) {
						contactComp.setValue(contactComp.getContactKey() + '=' + contactCache.get(contactComp.getContactKey()));
					}
				}
			}
		}
	}

	protected void clearCache(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		globalContext.setAttribute(ContactText.class.getCanonicalName(), null);
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		String js = "$('" + getInputContentName() + "').setText('');";

		out.println(XHTMLHelper.getInputOneSelect(getInputChoiceName(), getChoices(ctx), getContactKey(), js, true));
		out.println("<div class=\"line\"><textarea id=\"" + getInputContentName() + "\" name=\"" + getInputContentName() + "\">" + getContent(ctx) + "</textarea></div>");
		out.println("<div class=\"line\"><input type=\"text\" name=\"" + getInputKeyName() + "\" /><input type=\"submit\" name=\"" + getInputAddName() + "\" value=\"add\" /></div>");

		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		String previousValue = getValue();

		String key = requestService.getParameter(getInputKeyName(), "");
		String selectedKey = requestService.getParameter(getInputChoiceName(), "");
		String content = requestService.getParameter(getInputContentName(), "");

		Map<String, String> cache = getContactCache(ctx);
		if (key.trim().length() > 0 && requestService.getParameter(getInputAddName(), null) != null) {
			this.key = key;
			selectedKey = key;
		}
		if (selectedKey.trim().length() > 0) {
			this.key = selectedKey;
		}

		setValue(this.key + '=' + content);
		if (!content.equals(this.content) && content.trim().length() > 0) {
			cache.put(this.key, content);
			updateAllContent(ctx);
		}

		if (!getValue().equals(previousValue)) {
			setModify();
			setNeedRefresh(true);
			this.content = content;
		}

		return super.performEdit(ctx);
	}

	@Override
	public String getHexColor() {
		return TEXT_COLOR;
	}

	@Override
	public boolean isListable() {
		return true;
	}
}
