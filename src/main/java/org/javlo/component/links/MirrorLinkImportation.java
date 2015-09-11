/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class MirrorLinkImportation extends AbstractVisualComponent {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(MirrorLinkImportation.class.getName());

	public String getCurrentLanguage() {
		if (getValue().trim().length() == 0) {
			return null;
		}
		String[] allValues = getValue().split(";");
		return allValues[0];
	}

	public List<String> getCurrentType() {
		if (getValue().trim().length() == 0) {
			return null;
		}
		String[] allValues = getValue().split(";");
		List<String> result = new LinkedList<String>();
		if (allValues.length > 1) {
			for (int i = 1; i < allValues.length; i++) {
				result.add(allValues[i]);
			}
		}
		return result;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		out.println(getSpecialInputTag());

		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			String title = i18nAccess.getText("content.mirror-link-importation.language");
			out.println("<div class=\"edit\">");
			out.println("<div class=\"line\">");
			out.print("<label for=\"" + getLanguageName() + "\">" + title + "</label>");
			out.print(" : ");
			out.println(XHTMLHelper.getInputOneSelect(getLanguageName(), globalContext.getLanguages(), getCurrentLanguage()));
			title = i18nAccess.getText("content.mirror-link-importation.component");
			out.println("</div><div class=\"line\">");
			out.print("<label for=\"" + getLanguageName() + "\">" + title + "</label>");
			out.print(" : ");
			IContentVisualComponent[] compArray = ComponentFactory.getComponents(globalContext);
			List<IContentVisualComponent> comps = new LinkedList<IContentVisualComponent>();
			for (IContentVisualComponent element : compArray) {
				if (element.isVisible()) {
					comps.add(element);
				}
			}
			String[][] compLabel = new String[comps.size()][];
			Iterator<IContentVisualComponent> compsIterator = comps.iterator();
			for (int j = 0; j < compLabel.length; j++) {
				IContentVisualComponent comp = compsIterator.next();
				compLabel[j] = new String[2];
				compLabel[j][0] = comp.getType();
				compLabel[j][1] = i18nAccess.getText("content." + comp.getType());
			}
			List<String> currentTypeList = getCurrentType();
			String[] currentType = new String[currentTypeList.size()];
			currentTypeList.toArray(currentType);
			out.println(XHTMLHelper.getInputMultiSelect(getTypeName(), compLabel, currentType, null, null));
			out.println("</div></div>");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return writer.toString();
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	public String getLanguageName() {
		return "__" + getId() + ID_SEPARATOR + "language";
	}

	@Override
	public String getType() {
		return "mirror-link-importation";
	}

	public String getTypeName() {
		return "__" + getId() + ID_SEPARATOR + "language";
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ContentContext localContext = new ContentContext(ctx);
		localContext.setLanguage(getCurrentLanguage());
		MenuElement sourcePage = localContext.getCurrentPage();
		MenuElement targetPage = ctx.getCurrentPage();
		ComponentBean[] contentSource = sourcePage.getContent();
		ComponentBean[] contentTarget = targetPage.getContent();
		Map<String, String> idImported = new HashMap<String, String>();
		for (ComponentBean element : contentTarget) {
			if (element.getType().equals(MirrorComponent.TYPE)) {
				idImported.put(element.getValue(), element.getId());
			}
		}
		String parent = getId();
		for (int i = 0; i < contentSource.length; i++) {
			if (getCurrentType().contains(contentSource[i].getType())) {
				if (!idImported.keySet().contains(contentSource[i].getId())) {
					parent = MacroHelper.addContent(ctx.getRequestContentLanguage(), targetPage, parent, MirrorComponent.TYPE, contentSource[i].getId(), ctx.getCurrentEditUser());
				} else {
					parent = idImported.get(contentSource[i].getId());
				}
			}
		}
		return "";
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String newLg = requestService.getParameter(getLanguageName(), null);
		if (newLg != null) {
			if (!newLg.equals(getCurrentLanguage())) {
				setModify();
			}
			String[] types = requestService.getParameterValues(getTypeName(), new String[0]);
			if (types.length != getCurrentType().size()) {
				setModify();
			} else {
				for (int i = 0; i < types.length; i++) {
					if (!getCurrentType().contains(types[i])) {
						setModify();
					}
				}
			}
			String value = newLg;
			for (String type : types) {
				value = value + ";" + type;
			}
			setValue(value);

		}
		return null;
	}

}
