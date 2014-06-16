/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.fileupload.FileItem;
import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;

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
		out.println("<legend>"+getValue()+"</legend>");
		out.println("<ul class=\"file-list\">");
		if (getFolder(ctx).exists()) {
			out.println("<ul class=\"file-list\">");
			String firstClass = " class=\"first\"";
			for (File file : getFolder(ctx).listFiles()) {
				out.println("<li" + firstClass + "><a href=\"" + URLHelper.getFileURL(ctx, file) + "\">" + file.getName() + " (" + StringHelper.renderSize(file.length()) + ")</a></li>");
				firstClass = "";
			}
			out.println("</ul>");
		}
		out.println("<form id=\"upload-form-" + getId() + "\" class=\"upload-form\" enctype=\"multipart/form-data\" method=\"post\"><div class=\"field-wrapper\">");
		out.println("<input type=\"hidden\" name=\"webaction\" value=\"file-list.upload\" />");
		out.println("<input type=\"hidden\" name=\"" + IContentVisualComponent.COMP_ID_REQUEST_PARAM + "\" value=\"" + getId() + "\" />");
		out.println("<input type=\"file\" multiple=\"multiple\" name=\"files\" />");
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		out.println("<input type=\"submit\" value=\""+i18nAccess.getViewText("global.send")+"\" />");
		out.println("</div></form>");
		out.println("</fieldset>");
		out.close();
		return new String(outStream.toByteArray());

	}

	private File getFolder(ContentContext ctx) {
		return new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), ctx.getGlobalContext().getStaticConfig().getStaticFolder(), getType(), getId()));
	}

	@Override
	public String getActionGroupName() {
		return "file-list";
	}

	public static String performUpload(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		System.out.println("***** GenericFileList.performUpload : START."); //TODO: remove debug trace
		GenericFileList comp = (GenericFileList) ComponentHelper.getComponentFromRequest(ctx);
		for (FileItem fileItem : rs.getAllFileItem()) {
			System.out.println("***** GenericFileList.performUpload : name = "+fileItem.getName()); //TODO: remove debug trace
			File newFile = new File(URLHelper.mergePath(comp.getFolder(ctx).getAbsolutePath(), fileItem.getName()));
			InputStream in = null;
			try {
				in = fileItem.getInputStream();
				ResourceHelper.writeStreamToFile(in, newFile);
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
		for (File file : getFolder(ctx).listFiles()) {
			file.delete();
		}
		getFolder(ctx).delete();
	}

}
