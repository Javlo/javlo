package org.javlo.component.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.utils.CSVFactory;
import org.javlo.ztatic.StaticInfo;
import org.javlo.ztatic.StaticInfoBean;
import org.owasp.encoder.Encode;

public class CSVArrayComponent extends AbstractFileComponent {

	/**
	 * get the size of empty cell after the current cell (for colspan)
	 * 
	 * @param cell
	 * @param startCell
	 * @return
	 */
	private static int getEmptyLength(String[] cell, int startCell) {
		int out = 0;
		int i = startCell + 1;
		while (i < cell.length && cell[i].length() == 0) {
			out++;
			i++;
		}
		return out;
	}

	public static void main(String[] args) {
		String[] test1 = { "patrick", "barbara" };
		String[] test2 = { "patrick", "barbara", "", "", "coucou" };
		System.out.println(getEmptyLength(test1, 1));
		System.out.println(getEmptyLength(test1, 5));
		System.out.println(getEmptyLength(test2, 1));
		System.out.println(getEmptyLength(test2, 2));
	}
	
	protected String getImageLabelTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.array.label");
	}

	@Override
	public String createFileURL(ContentContext ctx, String url) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		String outURL = URLHelper.createStaticURL(ctx, staticConfig.getCSVFolder() + '/' + url).replace('\\', '/');
		return outURL;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}

	@Override
	public String getHexColor() {
		return TEXT_COLOR;
	}

	private Charset getCurrentEncoding(ContentContext ctx) {
		Charset charset = Charset.forName("utf-16"); // default encoding if encoding not found

		try {
			if (getEncoding().equals(DEFAULT_ENCODING)) {
				charset = Charset.forName(GlobalContext.getInstance(ctx.getRequest()).getDefaultEncoding());
			} else {
				charset = Charset.forName(getEncoding());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return charset;
	}

	@Override
	protected String getDeleteTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-file.delete-file");
	}

	@Override
	public String getFileDirectory(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		return URLHelper.mergePath(globalContext.getDataFolder(), getRelativeFileDirectory(ctx));
	}

	@Override
	protected String getImageChangeTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-file.change");
	}

	@Override
	protected String getImageUploadTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-file.add");
	}

	@Override
	public String getPreviewCode(ContentContext ctx) throws Exception {
		StringBuffer res = new StringBuffer();
		if ((getValue() != null) && (getValue().trim().length() > 0)) {
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());

			String url = URLHelper.mergePath(getDirSelected(), getFileName());
			url = URLHelper.createResourceURL(ctx, getPage(), staticConfig.getFileFolder() + '/' + url);
			res.append("<a href=\"");
			res.append(url);
			res.append("\">");
			if (getLabel().trim().length() == 0) {
				res.append(getFileName());
			} else {
				res.append(XHTMLHelper.textToXHTML(getLabel()));
			}
			String fullName = URLHelper.mergePath(getDirSelected(), getFileName());
			fullName = URLHelper.mergePath(staticConfig.getCSVFolder(), fullName);
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			fullName = URLHelper.mergePath(globalContext.getDataFolder(), fullName);
			res.append("&nbsp;" + XHTMLHelper.getFileIcone(ctx, getFileName()) + " (" + StringHelper.getFileSize(fullName) + ")</a>");
		} else {
			res.append("&nbsp; <!--FILE NOT DEFINED--> ");
		}
		return res.toString();
	}

	@Override
	protected String getRelativeFileDirectory(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getCSVFolder();
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {

		String thth = "double title";
		String thtd = "cols title";
		String tdth = "rows title";
		String tdtd = "no title";

		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			thth = i18nAccess.getText("content.csv-array.thth");
			thtd = i18nAccess.getText("content.csv-array.thtd");
			tdth = i18nAccess.getText("content.csv-array.tdth");
			tdtd = i18nAccess.getText("content.csv-array.tdtd");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new String[] { thth, thtd, tdth, tdtd };
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "th-th", "th-td", "td-th", "td-td" };
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			return i18nAccess.getText("content.csv-array.style-title");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "style";
	}

	@Override
	public String getType() {
		return "csv-array";
	}
	
	public File getFile(ContentContext ctx) {
		String basePath = URLHelper.mergePath(getFileDirectory(ctx), getDirSelected());
		return new File(URLHelper.mergePath(basePath, getFileName()));
	}
	
	@Override
	public List<File> getFiles(ContentContext ctx) {
		List<File> files = new LinkedList<File>();
		File file = getFile(ctx);
		if (file != null) {
			files.add(file);
		}
		return files;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		CSVFactory csvFactory;
		File csvFile = getFile(ctx);
		String colTH = "th";
		String rowTH = "th";
		String style = getStyle();
		boolean autoColSpan = true;
		if (style != null) {
			if (style.equals("th-td")) {
				autoColSpan = false;
				rowTH = "td";
			} else if (style.equals("td-th")) {
				colTH = "td";
			} else if (style.equals("td-td")) {
				rowTH = "td";
				colTH = "td";
			}
		}

		InputStream in = null;
		if (csvFile.exists() && !csvFile.isDirectory()) {
			in = new FileInputStream(csvFile);
		} else {
			return "<b>WARNING: file not found : " + csvFile + ". </b>";
		}

		try {
			csvFactory = new CSVFactory(in, null, getCurrentEncoding(ctx));
		} finally {
			ResourceHelper.closeResource(in);
		}

		StringWriter stringWriter = new StringWriter();
		stringWriter.append("<div " + getSpecialPreviewCssClass(ctx, getStyle(ctx) + " " + getType()) + getSpecialPreviewCssId(ctx) + ">");
		if (getLabel().trim().length() > 0) {
			stringWriter.append("<table class=\"" + getStyle(ctx) + "\"><caption>"+Encode.forHtml(getLabel())+"</caption>");
		} else {
			stringWriter.append("<table class=\"" + getStyle(ctx) + "\">");
		}

		String[][] array = csvFactory.getArray();

		if (array.length == 0) {
			return "<b>WARNING: no data found in csv file. (col)</b>";
		} else if (array[0].length == 0) {
			return "<b>WARNING: no data found in csv file. (row)</b>";
		}

		for (int i = 0; i < array.length; i++) {
			if (i % 2 == 1) {
				stringWriter.append("<tr class=\"row-" + i + " odd\">");
			} else {
				stringWriter.append("<tr class=\"row-" + i + "\" >");
			}
			for (int j = 0; j < array[i].length; j++) {
				String tag = "td";

				if (j == 0 || array[i][j].length() > 0 || !autoColSpan) {
					int colSpan = 1;
					if (autoColSpan) {
						colSpan = getEmptyLength(array[i], j) + 1;
					}
					if (i == 0) {
						tag = colTH;
					} else if (j == 0) {
						tag = rowTH;
					}

					String cssClass = "";
					String content = array[i][j];
					if (content.trim().length() == 0) {
						cssClass = " empty";
					}

					String colSpanHTML = "";
					if (colSpan > 1) {
						colSpanHTML = " colspan=\"" + colSpan + "\"";
					}
					if (j % 2 == 1) {
						stringWriter.append('<' + tag + " class=\"odd" + cssClass + "\"" + colSpanHTML + ">");
					} else {
						stringWriter.append('<' + tag + " class=\"even" + cssClass + "\"" + colSpanHTML + '>');
					}

					content = renderCell(content);

					stringWriter.append(content);
					stringWriter.append("</" + tag + '>');
				}
			}
			stringWriter.append("</tr>");
		}

		stringWriter.append("</table>");		
		StaticInfo staticInfo = getStaticInfo(ctx);
		if (staticInfo != null) {
			ContentContext infoCtx = staticInfo.getContextWithContent(ctx);			
			stringWriter.append(XHTMLHelper.renderStaticInfo(infoCtx, staticInfo));
		}
		stringWriter.append("</div>");

		return stringWriter.toString();
	}

	@Override
	protected boolean isFileNameValid(ContentContext ctx, String fileName) {

		return true;

		/*
		 * if (fileName == null) { return false; } return StringHelper.getFileExtension(fileName).toLowerCase().equals("csv");
		 */
	}

	@Override
	protected boolean needEncoding() {
		return true;
	}

	protected String renderCell(String content) {
		if (content.trim().length() == 0) {
			content = "&nbsp;";
		}
		return XHTMLHelper.autoLink(content);
	}

	@Override
	public int getPopularity(ContentContext ctx) {
		return 0;
	}
	
	@Override
	public boolean isUploadOnDrop() {
		return false;
	}
	
	@Override
	public String getFontAwesome() {	
		return "table";
	}
	
}
