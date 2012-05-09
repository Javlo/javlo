/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.portlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.portlet.Event;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.pluto.container.PortletContainer;
import org.javlo.actions.IAction;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.portlet.PortalContextImpl;
import org.javlo.portlet.PortletEventListener;
import org.javlo.portlet.PortletManager;
import org.javlo.portlet.PortletResponsePropertyListener;
import org.javlo.portlet.PortletURLProviderImpl;
import org.javlo.portlet.PortletWindowImpl;
import org.javlo.portlet.request.HttpServletRequestWrapper;
import org.javlo.portlet.request.HttpServletResponseWrapper;
import org.javlo.service.RequestService;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Parent;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * @author plemarchand
 */
public abstract class AbstractPortletWrapperComponent extends AbstractPropertiesComponent
		implements IAction, PortletResponsePropertyListener, PortletEventListener {

	public static final String PORTLET_ID_ATTR_NAME = "portlet_id";

	protected static Logger logger = Logger.getLogger(AbstractPortletWrapperComponent.class.getName());

	public static final String PORTLET_VALUE_FIELD = "portlet_value";

	// useless, actually, or maybe for a call to super for raw data...
	static final List<String> FIELDS = Arrays.asList(new String[] { PORTLET_VALUE_FIELD });


	@Override
	public List<String> getFields(ContentContext ctx) {
		return FIELDS;
	}

	@Override
	public final String getHeader() {
		return "portlet-config";
	}

	public String getActionGroupName() {
		return "portlet";
	}

	@Override
	public String getHexColor() {
		return IContentVisualComponent.DEFAULT_COLOR;
	}

	/**
	 * must match portlet name from portlet.xml
	 * 
	 * context path will be guessed from it, like PortletName -> portlet-name-portlet if different, precise in static config with <code>portal.portlet.PortletName=context-path</code>
	 * 
	 * @return portlet name as defined in corresponding portlet.xml
	 */
	public abstract String getPortletName();

	public abstract String getPortletValueChangedEventName();
	public abstract String getInitPortletValueEventName();
	public abstract String getDeletePortletEventName();

	public String getPortletValue(ContentContext ctx) {
		return getFieldValue(PORTLET_VALUE_FIELD);
	}

	protected void setPortletValue(String value) {
		setFieldValue(PORTLET_VALUE_FIELD, value);

		storeProperties();
		setModify();
	}
	
	/**
	 * to be overridden to manage custom portlet modes
	 * defaults to edit for edit render mode, view for others
	 * 
	 * @return the portlet modes supported by the current render mode, the first one being the default, never null
	 */
	public List<PortletMode> getPortletModes(int renderMode) {
		List<PortletMode> result = new ArrayList<PortletMode>();
		if (ContentContext.EDIT_MODE == renderMode) {
			result.add(PortletMode.EDIT);
		} else {
			result.add(PortletMode.VIEW);
		}
		return result;
	}
	
	/**
	 * interesting if portlet behavior depends on window id
	 * to be overridden to manage custom portlet modes
	 * defaults to ".work/" for edit and preview render modes, "" for others
	 * 
	 * @return a prefix to be added before standard id (host_name/page/path/componentId)
	 */
	public String getWindowIdPrefix(int renderMode, PortletMode portletMode) {
		if (ContentContext.EDIT_MODE == renderMode || ContentContext.PREVIEW_MODE == renderMode) {
			return ".work/";
		} else {
			return "";
		}
	}
	
	@Override
	public void delete(ContentContext ctx) {
		PortletManager pm = PortletManager.getInstance(ctx.getRequest().getSession().getServletContext());
		pm.deleteComponent(this, ctx);
		
		super.delete(ctx);
	}

	@Override
	public void refresh(ContentContext ctx) throws Exception {
		ServletContext application = ctx.getRequest().getSession().getServletContext();

		PortletWindowImpl pw = getPortletWindow(ctx);
		if (pw != null) {
			PortletContainer pc = PortletManager.getInstance(application).getPortletContainer();
			PortalContextImpl portalContext = (PortalContextImpl) pc.getContainerServices().getPortalContext();

			RequestService requestService = RequestService.getInstance(ctx.getRequest());
			if (requestService.getParameter(portalContext.getActionParamName() + getId(), null) != null) {
				pc.doAction(pw, new HttpServletRequestWrapper(ctx, getId()), ctx.getResponse());
			}
		}
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		out.println("<div class=\"" + getType() + "\">");

		String viewXHTML = renderPortlet(ctx);
		if (viewXHTML != null) {
			out.println(viewXHTML);
		} else {
			out.println("<p>content not available, please refresh or try again later...</p>");
		}

		out.println("</div>");
		out.close();
		return writer.toString();
	}

	/**
	 * @param ctx
	 * @return null if no portlet found
	 * @throws Exception
	 */
	public final String renderPortlet(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		PortletWindowImpl pw = getPortletWindow(ctx);
		if (pw != null) {
			ServletContext application = ctx.getRequest().getSession().getServletContext();
			PortletContainer pc = PortletManager.getInstance(application).getPortletContainer();
			PortalContextImpl portalContext = (PortalContextImpl) pc.getContainerServices().getPortalContext();

			StringWriter writer = new StringWriter();
			PrintWriter out = new PrintWriter(writer);
			HttpServletRequestWrapper hsrw = new HttpServletRequestWrapper(ctx, pw.getId().getStringId());

			String actionParamName = portalContext.getActionParamName();
			if ((PortletMode.EDIT.equals(pw.getPortletMode()) || "admin".equals(pw.getPortletMode().toString()))
					&& !WindowState.MAXIMIZED.equals(pw.getWindowState())) {
				
				StringWriter portletWriter = new StringWriter();

				// no action done, as edit normal -> adaptEditXHTML -> refresh
				pc.doRender(pw, hsrw, new HttpServletResponseWrapper(ctx.getResponse(), portletWriter));
				logger.log(Level.FINE, portletWriter.toString());

				String portletXMLResult = adaptPortletEditXHTML(portletWriter.toString(), actionParamName);
				out.println(portletXMLResult);
			} else {
				if (requestService.getParameter(actionParamName, null) != null) {
					String portletId = requestService.getParameter(PortletURLProviderImpl.PORTLET_ID_PARAM_NAME, null);
					if (getId().equals(portletId)) {
						pc.doAction(pw, hsrw, ctx.getResponse());
					}
				}
				pc.doRender(pw, hsrw, new HttpServletResponseWrapper(ctx.getResponse(), writer));
			}
			out.close();
			return writer.toString();
		} else {
			return null;
		}
	}

	public void renderPortletResource(ContentContext ctx) throws Exception {
		ServletContext application = ctx.getRequest().getSession().getServletContext();
		PortletContainer pc = PortletManager.getInstance(application).getPortletContainer();
		
		PortletWindowImpl pw = getPortletWindow(ctx);
		pc.doServeResource(pw, ctx.getRequest(), ctx.getResponse());
	}
	
	public final PortletWindowImpl getPortletWindow(ContentContext ctx) {
		PortletManager pm = PortletManager.getInstance(ctx.getRequest().getSession().getServletContext());
		PortletWindowImpl pw = pm.getPortletWindow(this, ctx);

		if (pw != null) {

			// legacy, windowID used instead
			ctx.getRequest().setAttribute(PORTLET_ID_ATTR_NAME, pw.getId().getStringId());

			// legacy, event based storage instead
			ctx.getRequest().setAttribute(pw.getId().getStringId(), getPortletValue(ctx));
		}
		return pw;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		out.println("<div class=\"" + getType() + "\">");

		String editXHTML = renderPortlet(ctx);
		if (editXHTML != null) {
			out.println(editXHTML);
		} else {
			out.println("<p>" + getType() + " not available, report to administrator</p>");
		}
		out.println("</div>");
		out.close();
		return writer.toString();
	}

	protected String adaptPortletEditXHTML(String portletXML, String actionParamName) {
		String result;

		StringReader reader = new StringReader(portletXML);
		SAXBuilder sb = new SAXBuilder();
		try {
			Collection<Element> forms = new ArrayList<Element>();
			Map<String, Element> allFormInputs = new HashMap<String, Element>();

			Document doc = sb.build(reader);
			for (Object elemObj : doc.getContent(new ElementFilter())) {
				searchElements((Element) elemObj, forms, Arrays.asList("form"));
			}

			String[] names = { "input", "textarea", "select" };
			List<String> inputNames = Arrays.asList(names);

			// TODO: check buttons or other controls that could submit a form
			String[] types = { "submit", "image" };
			List<String> submitTypes = Arrays.asList(types);

			for (Element form : forms) {
				Map<String, Element> formInputs = new HashMap<String, Element>();
				Set<Element> inputSubmits = new HashSet<Element>();
				Map<String, String> scriptParams = new HashMap<String, String>();

				Collection<Element> inputs = new ArrayList<Element>();
				searchElements(form, inputs, inputNames);
				for (Element input : inputs) {
					String inputName = input.getAttributeValue("name");
					String inputType = input.getAttributeValue("type");
					if (inputName != null) {
						input.setAttribute("name", inputName + getId());
						if (allFormInputs.containsKey(inputName)) {
							if ("select".equals(input.getName())) {

								// TODO: handle select options
							} else if (!("radio".equals(inputType) || "checkbox".equals(inputType))) {
								scriptParams.put(inputName + getId(), StringEscapeUtils.escapeXml(input.getAttributeValue("value")));
								input.detach();
							}
						}
						allFormInputs.put(inputName, input);
						formInputs.put(inputName, input);
					}
					if (submitTypes.contains(inputType)) {
						inputSubmits.add(input);
					}
				}

				String actionAttr = StringEscapeUtils.unescapeXml(form.getAttributeValue("action"));
				if (actionAttr.indexOf('?') > -1) {
					StringTokenizer stq = new StringTokenizer(actionAttr.substring(actionAttr.indexOf('?') + 1), "&");
					while (stq.hasMoreTokens()) {
						StringTokenizer stp = new StringTokenizer(stq.nextToken(), "=");
						try {
							String paramName = stp.nextToken();
							if (!formInputs.containsKey(paramName)) {
								if (allFormInputs.containsKey(paramName)) {
									scriptParams.put(paramName, StringEscapeUtils.escapeXml(stp.nextToken()));
								} else {
									Element hiddenInput = new Element("input");
									hiddenInput.setAttribute(new Attribute("type", "hidden"));
									hiddenInput.setAttribute(new Attribute("name", paramName + getId()));
									hiddenInput.setAttribute(new Attribute("value", StringEscapeUtils.escapeXml(stp.nextToken())));

									// TODO: handle multiple value get param
									formInputs.put(paramName, hiddenInput);
									allFormInputs.put(paramName, hiddenInput);

									form.addContent(0, hiddenInput);
								}

								// if form has the action param as input, do nothing (as user knows what he does)
								// otherwise replace value, as get params overrides post ones
							} else if (!paramName.equals(actionParamName)) {

								// TODO: handle textarea and select types ??? should never happen, anyway...
								formInputs.get(paramName).setAttribute("value", StringEscapeUtils.escapeXml(stp.nextToken()));
							}
						} catch (NoSuchElementException e) {
							// ignore parameter
						}
					}
				}

				// TODO: check all events like onchange... ?
				String onSubmit = form.getAttributeValue("onsubmit");
				if (onSubmit != null) {
					for (String paramName : allFormInputs.keySet()) {
						if (onSubmit.contains(paramName)) {
							onSubmit = onSubmit.replace(paramName, paramName + getId());
						}
					}
				} else {
					onSubmit = "";
				}

				if (!inputSubmits.isEmpty()) {
					String script = "";
					if (!scriptParams.isEmpty()) {
						for (String scriptParam : scriptParams.keySet()) {
							script = script + "this.form." + scriptParam + getId() + ".value='" + scriptParams.get(scriptParam) + "';";
						}
					}

					for (Element inputSubmit : inputSubmits) {
						String onClick = inputSubmit.getAttributeValue("onclick");
						if (onClick != null) {
							for (String paramName : allFormInputs.keySet()) {
								if (onClick.contains('.' + paramName)) {
									onClick = onClick.replace('.' + paramName, '.' + paramName + getId());
								}
							}
							onClick = script + onClick;
						} else {
							onClick = script;
						}

						if (onClick.length() > 0 && !onClick.endsWith(";")) {
							onClick = onClick + ";";
						}
						onClick = onClick + onSubmit;
						if (onClick.length() > 0) {
							inputSubmit.setAttribute("onclick", onClick);
						}
					}
				} else {

					// TODO: check potential javascript submit
					logger.log(Level.SEVERE, "cannot set onclick attribute without submit on form: " + form);
				}

				Parent parent = form.getParent();
				int position = parent.indexOf(form);
				if (form.isRootElement()) {
					doc.addContent(position, form.removeContent());
				} else {
					((Element) parent).addContent(position, form.removeContent());
				}
				form.getParent().removeContent(form);
			}

			Format format = Format.getRawFormat();
			format.setOmitDeclaration(true);
			format.setExpandEmptyElements(true);
			XMLOutputter xo = new XMLOutputter(format);

			StringWriter sw = new StringWriter();
			xo.output(doc, sw);
			result = sw.toString();
		} catch (JDOMException e) {
			// TODO: try to make the work full text
			logger.log(Level.SEVERE, "error parsing portlet", e);
			result = "<p>error parsing portlet</p>";
		} catch (IOException e) {
			logger.log(Level.SEVERE, "error rendering portlet", e);
			result = "<p>error rendering portlet</p>";
		}
		return result;
	}

	/**
	 * recursive but stops when matches name
	 * 
	 * @param root the Element below which to search for elements
	 * @param result the list to be populated
	 * @param elemNames the list of tag names to match
	 */
	private void searchElements(Element root, Collection<Element> result, List<String> elemNames) {
		if (elemNames != null && elemNames.contains(root.getName())) {
			result.add(root);
		} else {
			for (Object childObj : root.getContent(new ElementFilter())) {
				searchElements((Element) childObj, result, elemNames);
			}
		}
	}

	@Override
	public final void onPropertySet(HttpServletRequest req, String name, String value) {
		if (getId().equals(name)) {
			setPortletValue(value);
		}
	}

	@Override
	public final void onEvent(HttpServletRequest req, Event event) {
		if (event.getName().equals(getPortletValueChangedEventName())) {
			setPortletValue(event.getValue().toString());
		}
	}
}
