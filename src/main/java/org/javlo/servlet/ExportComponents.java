package org.javlo.servlet;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.fields.Field;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.template.TemplateFactory;
import org.javlo.utils.CSVFactory;

public class ExportComponents extends HttpServlet {

	private static Logger logger = Logger.getLogger(ExportComponents.class.getName());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
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
				if (componentType.toLowerCase().endsWith(".html")) {
					ctx.setRenderMode(ContentContext.VIEW_MODE);
					String compId = StringHelper.getFileNameWithoutExtension(StringHelper.getFileNameFromPath(request.getRequestURI()));
					ContentService content = ContentService.getInstance(ctx.getRequest());
					IContentVisualComponent comp = content.getComponent(ctx, compId);
					ctx.setFree(false);
					comp.initContent(ctx);
					String xhtml = comp.getXHTMLCode(ctx);
					ResourceHelper.writeStringToStream(xhtml, response.getOutputStream());
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
						response.setStatus(HttpServletResponse.SC_NOT_FOUND, "lang not found.");
						return;
					}
				}
				componentType = splittedPath[1];

			} else if (splittedPath.length != 1) {
				logger.warning("bad path structure : " + componentType);
				response.setStatus(HttpServletResponse.SC_NOT_FOUND, "bad path structure : $lang/$comp_type.");
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
						values.add(comp.getStyle(ctx));
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
						values.add(comp.getStyle(ctx));
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
