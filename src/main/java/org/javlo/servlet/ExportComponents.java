package org.javlo.servlet;

import org.apache.commons.lang3.StringUtils;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.fields.Field;
import org.javlo.helper.*;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.template.TemplateFactory;
import org.javlo.utils.CSVFactory;
import org.javlo.utils.Cell;
import org.javlo.utils.XLSTools;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public class ExportComponents extends HttpServlet {
	
	private static final String TEMP_DYANAMIC_COMPONENT_FOLDER = "/components.temp";

	private static Logger logger = Logger.getLogger(ExportComponents.class.getName());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}
	
	private void renderDynamicComponent(String type, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response, false);		
		File compFile=null;
		for (File comp : ctx.getGlobalContext().getExternComponents()) {
			if (StringHelper.getFileNameWithoutExtension(comp.getName()).equals(type)) {
				compFile=comp;
			}
		}
		if (compFile == null) {
			logger.warning("document not found.");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		File tempFolder = new File(getServletContext().getRealPath(URLHelper.mergePath(TEMP_DYANAMIC_COMPONENT_FOLDER, ctx.getGlobalContext().getContextKey())));
		if (!tempFolder.exists()) {
			tempFolder.mkdirs();
		}
		File destFile = new File(URLHelper.mergePath(tempFolder.getAbsolutePath(), compFile.getName()));
		ResourceHelper.copyFile(compFile, destFile, true);
		DynamicComponent dc = new DynamicComponent();
		dc.init(new ComponentBean(type, ResourceHelper.loadStringFromFile(destFile), "en"), ctx);
		response.getWriter().print(dc.getViewXHTMLCode(ctx));
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {

		RequestService rs = RequestService.getInstance(request);

		try {

			ContentContext ctx = ContentContext.getContentContext(request, response, false);
			ctx.setArea(null);
			ctx.setFree(true);
			ctx.setExport(true);

			String componentType = request.getPathInfo();
			if (!componentType.toLowerCase().endsWith(".csv")) {
				if (componentType.toLowerCase().endsWith(".html") || componentType.toLowerCase().endsWith(".js")) {
					response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);
					String compId = StringHelper.getFileNameWithoutExtension(StringHelper.getFileNameFromPath(request.getRequestURI()));
					if (compId != null) {

						if (!StringHelper.isEmpty(rs.getParameter("mode"))) {
							ctx.setRenderMode(Integer.parseInt(rs.getParameter("mode")));
							logger.info("change render mode : "+Integer.parseInt(rs.getParameter("mode")));
						}

						ContentService content = ContentService.getInstance(ctx.getRequest());
						ctx.setAbsoluteURL(true);
						IContentVisualComponent comp = content.getComponent(ctx, compId);

						if (comp == null) { // if not found in view mode -> search in preview mode.
							logger.warning("component not found : "+compId+" -> try in preview mode");
							ctx.setRenderMode(ContentContext.PREVIEW_MODE);
							comp = content.getComponent(ctx, compId);
						}
						if (comp == null) {
							logger.warning("component not found : "+compId);
							response.setStatus(HttpServletResponse.SC_NOT_FOUND);
							return;
						} else {

							if (!ctx.isAsViewMode()) {
								if (!SecurityHelper.userAccessPage(ctx, ctx.getCurrentUser(), comp.getPage())) {
									logger.severe("User not allowed to change mode access to this page : " + comp.getPage().getName());
									response.setStatus(HttpServletResponse.SC_FORBIDDEN);
									return;
								}
							}

							if (!comp.getPage().isReadAccess(ctx, ctx.getCurrentUser())) {
								logger.severe("User not read access to this page : " + comp.getPage().getName());
								response.setStatus(HttpServletResponse.SC_FORBIDDEN);
								return;
							}
							ctx.setFree(false);
							ctx.setCurrentTemplate(TemplateFactory.getTemplate(ctx, comp.getPage()));
							// comp.initContent(ctx);

							System.out.println("###### mode = "+ctx.getRenderMode());
							String xhtml = comp.getXHTMLCode(ctx);
							if (xhtml == null) {
								logger.severe("content not found on component : " + compId);
								response.setStatus(HttpServletResponse.SC_NOT_FOUND);
								return;
							}
							ResourceHelper.writeStringToStream(xhtml, response.getOutputStream(), ContentContext.CHARACTER_ENCODING);
						}
					}
				} else if (componentType.toLowerCase().endsWith(".xlsx")) {
					response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
					String[] splittedPath = StringUtils.split(request.getPathInfo(), '/');
					GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
					if (splittedPath.length > 1) {
						if (splittedPath[0].length() == 2) {
							if (!globalContext.getContentLanguages().contains(splittedPath[0])) {
								logger.warning("bad path structure : " + componentType);
								response.setStatus(HttpServletResponse.SC_NOT_FOUND);
								return;
							}
						}
						componentType = splittedPath[1];

					} else if (splittedPath.length != 1) {
						logger.warning("bad path structure : " + componentType);
						response.setStatus(HttpServletResponse.SC_NOT_FOUND);
						return;
					}
					componentType = componentType.replace(".xlsx", "").replace("/", "");
					ContentService content = ContentService.getInstance(request);
					Cell[][] cells = ComponentHelper.componentsToArray(ctx, content.getAllContent(ctx), componentType);
					XLSTools.writeXLSX(cells, response.getOutputStream());
				} else {
					logger.warning("bad format : " + componentType);
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
			}

			response.setContentType("text/csv");

			String[] splittedPath = StringUtils.split(request.getPathInfo(), '/');
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			String lg = globalContext.getDefaultLanguage();
			if (splittedPath.length > 1) {
				if (splittedPath[0].length() == 2) {
					if (globalContext.getContentLanguages().contains(splittedPath[0])) {
						lg = splittedPath[0];
					} else {
						logger.warning("bad path structure : " + componentType);
						response.setStatus(HttpServletResponse.SC_NOT_FOUND);
						return;
					}
				}
				componentType = splittedPath[1];

			} else if (splittedPath.length != 1) {
				logger.warning("bad path structure : " + componentType);
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			componentType = componentType.replace(".csv", "").replace("/", "");

			ContentService content = ContentService.getInstance(request);

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), Charset.forName(rs.getParameter("encoding", ContentContext.CHARACTER_ENCODING))));

			List<IContentVisualComponent> components = content.getAllContent(ctx);
			boolean firstLine = true;
			for (IContentVisualComponent comp : components) {
				if (comp.getType().equals(componentType)) {
					if (comp instanceof DynamicComponent) {
						DynamicComponent dcomp = (DynamicComponent) comp;

						if (firstLine) {
							firstLine = false;
							List<String> values = new LinkedList<String>();
							values.add("page");
							values.add("authors");
							values.add("style");
							values.add("area");
							List<Field> fields = dcomp.getFields(ctx);
							for (Field field : fields) {
								values.add(field.getName());
							}
							String csvLine = CSVFactory.exportLine(values, rs.getParameter("separator", ","));
							out.append(csvLine);
							out.newLine();
						}

						List<String> values = new LinkedList<String>();
						values.add(comp.getPage().getName());
						values.add(comp.getAuthors());
						values.add(comp.getComponentCssClass(ctx));
						values.add(comp.getArea());
						List<Field> fields = dcomp.getFields(ctx);
						for (Field field : fields) {
							values.add(field.getValue(new Locale(lg)));
						}
						out.append(CSVFactory.exportLine(values, rs.getParameter("separator", ",")));
						out.newLine();
					} else {
						if (firstLine) {
							firstLine = false;
							List<String> values = new LinkedList<String>();
							values.add("page");
							values.add("authors");
							values.add("value");
							values.add("style");
							values.add("area");
							String csvLine = CSVFactory.exportLine(values, rs.getParameter("separator", ","));
							out.append(csvLine);
							out.newLine();
						}
						List<String> values = new LinkedList<String>();
						values.add(comp.getPage().getName());
						values.add(comp.getAuthors());
						values.add(comp.getValue(ctx));
						values.add(comp.getComponentCssClass(ctx));
						values.add(comp.getArea());
						String csvLine = CSVFactory.exportLine(values, rs.getParameter("separator", ","));
						out.append(csvLine);
						out.newLine();
					}
				}
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}

	}
}
