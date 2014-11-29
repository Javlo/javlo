/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.Comparator.FileComparator;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.user.User;
import org.javlo.ztatic.StaticInfo;

/**
 * @author pvandermaesen
 */
public class GenericFileList extends AbstractVisualComponent implements IAction {

	@Override
	public String getType() {
		return "file-list";
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<fieldset>");
		out.println("<legend>" + getValue() + "</legend>");		
		if (getFolder(ctx).exists()) {
			out.println("<table class=\"file-list\">");
			String firstClass = " class=\"first\"";
			List<File> files = new LinkedList<File>(Arrays.asList(getFolder(ctx).listFiles()));
			Collections.sort(files, new FileComparator(FileComparator.LASTMODIFIED, true));
			for (File file : files) {
				StaticInfo info = StaticInfo.getInstance(ctx, file);
				out.print("<tr><td" + firstClass + "><a href=\"" + URLHelper.getFileURL(ctx, file) + "\">" + file.getName() + " (" + StringHelper.renderSize(file.length()) + ")</a></td>");
				out.println("<td class=\"date\">" + StringHelper.renderTime(new Date(file.lastModified())) + "</td>");
				String authors = info.getAuthors(ctx);
				out.println("<td class=\"authors\">" + authors + "</td>");
				out.println("<td class=\"action\">");
				if (ctx.getCurrentEditUser() != null && ctx.getCurrentEditUser().getLogin().equals(authors) && !ctx.isAsPageMode()) {
					Map<String, String> params = new HashMap<String, String>();
					params.put("webaction", "file-list.delete");
					params.put("fileCode", StringHelper.encryptPassword(file.getName()));
					params.put(IContentVisualComponent.COMP_ID_REQUEST_PARAM, getId());
					String link = URLHelper.createURL(ctx, params);
					I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
					out.println("<a class=\"delete\" href=\"" + link + "\">" + i18nAccess.getViewText("global.delete") + "</a>");
				} else {
					out.print("&nbsp;");
				}
				out.println("</td>");

				out.println("</tr>");
				firstClass = "";
			}
			out.println("</table>");
		}
		if (!ctx.isAsPageMode()) {
			out.println("<form id=\"upload-form-" + getId() + "\" class=\"upload-form\" enctype=\"multipart/form-data\" method=\"post\"><div class=\"field-wrapper\">");
			out.println("<input type=\"hidden\" name=\"webaction\" value=\"file-list.upload\" />");
			out.println("<input type=\"hidden\" name=\"" + IContentVisualComponent.COMP_ID_REQUEST_PARAM + "\" value=\"" + getId() + "\" />");
			out.println("<input type=\"file\" multiple=\"multiple\" name=\"files\" />");
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			out.println("<input type=\"submit\" value=\"" + i18nAccess.getViewText("global.send") + "\" />");
			out.println("</div></form>");
		}
		out.println("</fieldset>");
		out.close();
		return new String(outStream.toByteArray());

	}

	@Override
	public String getEmptyXHTMLCode(ContentContext ctx) throws Exception {
		return getViewXHTMLCode(ctx);
	}

	private File getFolder(ContentContext ctx) {
		return new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), ctx.getGlobalContext().getStaticConfig().getStaticFolder(), getType(), getId()));
	}

	@Override
	public String getActionGroupName() {
		return "file-list";
	}

	public static String performDelete(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String fileCode = rs.getParameter("fileCode", null);
		GenericFileList comp = (GenericFileList) ComponentHelper.getComponentFromRequest(ctx);
		for (File file : comp.getFolder(ctx).listFiles()) {
			String currentFileCode = StringHelper.encryptPassword(file.getName());
			if (currentFileCode.equals(fileCode)) {
				StaticInfo info = StaticInfo.getInstance(ctx, file);
				String authors = info.getAuthors(ctx);
				if (ctx.getCurrentEditUser().getLogin().equals(authors)) {
					file.delete();
				}
			}
		}
		return null;
	}

	public static String performUpload(RequestService rs, ContentContext ctx, MessageRepository messageRepository, User user, I18nAccess i18nAccess) throws Exception {
		if (user == null) {
			return "security error";
		}
		GenericFileList comp = (GenericFileList) ComponentHelper.getComponentFromRequest(ctx);
		for (FileItem fileItem : rs.getAllFileItem()) {
			File newFile = new File(URLHelper.mergePath(comp.getFolder(ctx).getAbsolutePath(), fileItem.getName()));
			newFile = ResourceHelper.getFreeFileName(newFile);
			InputStream in = null;
			try {
				in = fileItem.getInputStream();
				ResourceHelper.writeStreamToFile(in, newFile);
				StaticInfo info = StaticInfo.getInstance(ctx, newFile);
				info.setAuthors(ctx, user.getLogin());
				PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				ResourceHelper.closeResource(in);
			}
		}
		return null;
	}

	@Override
	public void delete(ContentContext ctx) {
		super.delete(ctx);
		if (getFolder(ctx) != null && getFolder(ctx).exists())
		for (File file : getFolder(ctx).listFiles()) {
			file.delete();
		}
		getFolder(ctx).delete();
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return IContentVisualComponent.COMPLEXITY_STANDARD;
	}

}
