package org.javlo.component.web2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

import org.apache.commons.fileupload.FileItem;
import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.io.SessionFolder;
import org.javlo.service.RequestService;
import org.javlo.ztatic.StaticInfo;

public class UploadFileComponent extends AbstractVisualComponent implements IAction {

	private static Logger logger = Logger.getLogger(UploadFileComponent.class.getName());

	public static final String TYPE = "upload-file";

	public static final String SESSION_IMAGE = "session-image";

	private String[] modes = new String[] { SESSION_IMAGE };

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getActionGroupName() {
		return TYPE;
	}

	public static String performUpload(ContentContext ctx, RequestService rs) throws Exception {
		IContentVisualComponent comp = ComponentHelper.getComponentFromRequest(ctx);
		if (comp.getValue(ctx).equals(SESSION_IMAGE)) {
			FileItem fileItem = rs.getFileItem("_image-" + comp.getId());
			if (fileItem != null) {
				try (InputStream in = fileItem.getInputStream()) {
					logger.info("upload : " + fileItem.getName());
					if (StringHelper.isImage(fileItem.getName())) {
						SessionFolder.getInstance(ctx).addImage("session-image." + StringHelper.getFileExtension(fileItem.getName()).toLowerCase(), in);
					} else {
						return "bad image format.";
					}
				}
			}
		} else {
			logger.severe("bad session : " + comp.getValue(ctx));
		}
		return null;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println(XHTMLHelper.getInputOneSelect(getContentName(), modes, getValue()));
		out.close();
		return new String(outStream.toByteArray());
	}

	private String getJs() {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<script>");
		out.println("function imageClick(img) {");
		out.println("    let bounds=img.getBoundingClientRect();");
		out.println("    var left=bounds.left;");
		out.println("    var top=bounds.top;");
		out.println("    var x = event.pageX - left - window.scrollX;");
		out.println("    var y = event.pageY - top - window.scrollY;");
		out.println("    var cw=img.clientWidth;");
		out.println("    var ch=img.clientHeight;");
		out.println("    var iw=img.naturalWidth;");
		out.println("    var ih=img.naturalHeight;");
		out.println("    var px=x/cw*iw;");
		out.println("    var py=y/ch*ih;");
		out.println("	var focusRealX = px * 1000 / iw;");
		out.println("	var focusRealY = py * 1000 / ih;");
		out.println("	document.getElementById('posx-" + getId() + "').value=focusRealX;");
		out.println("	document.getElementById('posy-" + getId() + "').value=focusRealY;");
		out.println("	document.getElementById('upload-form-focus-" + getId() + "').submit();");
		out.println("  };");
		out.println("</script>");
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		prepareView(ctx);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		String title = "";
		String descriptionPreview = "";
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
		if (getValue().equals(SESSION_IMAGE)) {
			title = i18nAccess.getViewText("upload-file.image.title");
			descriptionPreview = i18nAccess.getViewText("upload-file.image.focus");
		}

		out.println(getJs());
		out.println("<h2>" + title + "</h2>");
		out.println("<form id=\"upload-form-" + getId() + "\" method=\"post\" enctype=\"multipart/form-data\">");
		out.println("<input type=\"hidden\" name=\"webaction\" value=\""+TYPE+".upload\">");
		out.println("<input type=\"hidden\" name=\""+IContentVisualComponent.COMP_ID_REQUEST_PARAM+"\" value=\""+getId()+"\">");
		out.println("<input class=\"form-control\" type=\"file\" name=\"_image-" + getId() + "\" />");
		out.println("<button class=\"btn btn-secondary\" type=\"submit\">"+i18nAccess.getViewText("global.upload")+"</button>");
		out.println("</form>");

		String previewUrl = null;
		File file = null;
		if (getValue().equals(SESSION_IMAGE)) {
			file = SessionFolder.getInstance(ctx).getImage();
			if (file != null) {
				previewUrl = URLHelper.createMediaURL(ctx, file);
			}
		}
		if (previewUrl != null && file != null) {
			SessionFolder sessionFolder = SessionFolder.getInstance(ctx);
			StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
			out.println("<p class=\"description-preview\">" + descriptionPreview + "</p>");
			out.println("<form id=\"upload-form-focus-" + getId() + "\" method=\"post\">");
			out.println("<input type=\"hidden\" name=\"webaction\" value=\"view.updatefocus\">");
			out.println("<input type=\"hidden\" name=\"image_path\" value=\"" + URLHelper.mergePath(SessionFolder.SESSION_FOLDER, sessionFolder.getSessionId()) + "\">");
			out.println("<input type=\"hidden\" id=\"posx-" + getId() + "\" name=\"posx-" + sessionFolder.getImageFileId() + "\" value=\"" + staticInfo.getFocusZoneX(ctx) + "\">");
			out.println("<input type=\"hidden\" id=\"posy-" + getId() + "\" name=\"posy-" + sessionFolder.getImageFileId() + "\" value=\"" + staticInfo.getFocusZoneY(ctx) + "\">");
			out.println("<div class=\"focal-image\" stle=\"position: relative;\">");
			out.println("<img style=\"cursor: crosshair;\" src=\"" + previewUrl + "\" onclick=\"imageClick(this);\" />");
			out.println("</form>");
			out.println("</div>");
		}
		
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String getIcon() {
		return "bi bi-upload";
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_ADMIN;
	}

}
