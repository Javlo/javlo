package org.javlo.component.files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.javlo.component.core.IUploadResource;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.file.FileAction;
import org.javlo.module.file.FileBean;
import org.javlo.service.RequestService;
import org.javlo.user.UserSecurity;
import org.javlo.ztatic.StaticInfo;

public class FileFinder extends AbstractPropertiesComponent implements IUploadResource {

	public static final String TYPE = "file-finder";

	private static String SORT_NAME = "sort_name";
	private static String SORT_TITLE = "sort_title";
	private static String SORT_DATE = "sort_creation_date";
	private static String SORT_MODIFDATE = "sort_date";
	private static String SORT_NAME_DESC = "sort_name_desc";
	private static String SORT_TITLE_DESC = "sort_title_desc";
	private static String SORT_DATE_DESC = "sort_creation_date_desc";
	private static String SORT_MODIFDATE_DESC = "sort_date_desc";

	private static String[] styleList = new String[] { SORT_NAME, SORT_TITLE, SORT_DATE, SORT_MODIFDATE, SORT_NAME_DESC, SORT_TITLE_DESC, SORT_DATE_DESC, SORT_MODIFDATE_DESC };

	private static class FileFilter {
		private ContentContext ctx = null;
		private String text = null;
		private List<String> ext = Collections.emptyList();
		private List<String> noext = Collections.emptyList();
		private List<String> tags = Collections.emptyList();
		private File root;
		private boolean acceptDirectory = false;

		public static FileFilter getInstance(ContentContext ctx) {
			FileFilter outFilter = new FileFilter();
			outFilter.ctx = ctx;
			outFilter.text = ctx.getRequest().getParameter("text");
			outFilter.root = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), ctx.getGlobalContext().getStaticConfig().getFileFolder()));
			return outFilter;
		}

		public boolean match(ContentContext ctx, StaticInfo file) {
			if (file == null || file.getFile() == null) {
				return false;
			} else {
				if (acceptDirectory || !file.getFile().isDirectory()) {
					if (UserSecurity.isCurrentUserCanRead(ctx, file) && file.isShared(ctx)) {
						try {
							String fileExt = StringHelper.getFileExtension(file.getFile().getName());
							if (ext.size() == 0 || ext.contains(fileExt)) {
								if (noext.size() == 0 || !noext.contains(fileExt)) {
									if (tags.size() == 0 || !Collections.disjoint(file.getTags(ctx), tags)) {
										if (file.getFile().getCanonicalPath().startsWith(getRoot().getCanonicalPath())) {
											return text == null || text.trim().length() == 0 || (file.getFile().getAbsolutePath() + ' ' + file.getTitle(ctx) + ' ' + file.getDescription(ctx)).contains(text);
										}
									}
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
							return false;
						}
					}
				}
			}
			return false;
		}

		public File getRoot() {
			if (root == null) {

			}
			return root;
		}

		public List<String> getExt() {
			return ext;
		}

		public void setExt(List<String> ext) {
			this.ext = ext;
		}

		public List<String> getNoext() {
			return noext;
		}

		public void setNoext(List<String> noext) {
			this.noext = noext;
		}

		public void setRoot(File root) {
			this.root = root;
		}

		public List<String> getTags() {
			return tags;
		}

		public void setTags(List<String> tags) {
			this.tags = tags;
		}
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return styleList;
	}

	protected List<FileBean> getFileList(ContentContext ctx, FileFilter filter) throws Exception {
		Map<String, FileBean> fileWithRef = new HashMap<String, FileBean>();
		List<FileBean> outFileList = new LinkedList<FileBean>();
		// Set<String> ref = new HashSet<String>();
		for (File file : ResourceHelper.getAllFiles(filter.getRoot(), null)) {
			StaticInfo info = StaticInfo.getInstance(ctx, file);
			if (filter.match(ctx, info)) {
				StaticInfo.ReferenceBean refBean = info.getReferenceBean(ctx);				
				FileBean fileBean = null;
				if (refBean != null) {
					fileBean = fileWithRef.get(refBean.getReference());
					if (fileBean != null) {
						fileBean.addTranslation(new FileBean(ctx, file, refBean.getLanguage()));						
					}
				}
				if (fileBean == null) {
					if (refBean == null) {
						fileBean = new FileBean(ctx, info);
					} else {
						fileBean = new FileBean(ctx, file, ctx.getRequestContentLanguage());
						fileWithRef.put(refBean.getReference(), fileBean);
						fileBean.addTranslation(new FileBean(ctx, file, refBean.getLanguage()));
					}
					outFileList.add(fileBean);					
				}
			}
			if (getStyle().contentEquals(SORT_NAME)) {
				Collections.sort(outFileList, new FileBean.FileBeanComparator(ctx, 2, false));
			} else if (getStyle().contentEquals(SORT_DATE)) {
				Collections.sort(outFileList, new FileBean.FileBeanComparator(ctx, 4, false));
			} else if (getStyle().contentEquals(SORT_MODIFDATE)) {
				Collections.sort(outFileList, new FileBean.FileBeanComparator(ctx, -1, false));
			} else if (getStyle().contentEquals(SORT_TITLE)) {
				Collections.sort(outFileList, new FileBean.FileBeanComparator(ctx, 3, false));
			} else if (getStyle().contentEquals(SORT_NAME_DESC)) {
				Collections.sort(outFileList, new FileBean.FileBeanComparator(ctx, 2, true));
			} else if (getStyle().contentEquals(SORT_DATE_DESC)) {
				Collections.sort(outFileList, new FileBean.FileBeanComparator(ctx, 4, true));
			} else if (getStyle().contentEquals(SORT_MODIFDATE_DESC)) {
				Collections.sort(outFileList, new FileBean.FileBeanComparator(ctx, -1, true));
			} else if (getStyle().contentEquals(SORT_TITLE_DESC)) {
				Collections.sort(outFileList, new FileBean.FileBeanComparator(ctx, 3, true));
			}
		}

		return outFileList;
	}

	private List<String> FIELDS = Arrays.asList(new String[] { "root", "tags", "ext", "noext" });

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		FileFilter filter = FileFilter.getInstance(ctx);
		filter.setTags(getTags());
		filter.setExt(StringHelper.stringToCollection(getFieldValue("ext"), ","));
		filter.setNoext(StringHelper.stringToCollection(getFieldValue("noext"), ","));
		filter.setRoot(new File(URLHelper.mergePath(filter.getRoot().getCanonicalPath(), getFieldValue("root"))));
		ctx.getRequest().setAttribute("filter", filter);
		ctx.getRequest().setAttribute("files", getFileList(ctx, filter));
	}

	@Override
	public String getType() {
		return TYPE;
	}

	private List<String> getTags() {
		return StringHelper.stringToCollection(getFieldValue("tags"), getListSeparator());
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		if (ctx.getRequest().getParameter("path") != null) {
			String newFolder = URLHelper.removeStaticFolderPrefix(ctx, ctx.getRequest().getParameter("path"));
			newFolder = '/' + newFolder.replaceFirst("/" + ctx.getGlobalContext().getStaticConfig().getFileFolderName() + '/', "");
			if (newFolder.trim().length() > 1 && !getFieldValue("root").equals(newFolder)) {
				setFieldValue("root", newFolder);
			}
		}

		Map<String, String> filesParams = new HashMap<String, String>();
		String path = URLHelper.mergePath(FileAction.getPathPrefix(ctx), StaticConfig.getInstance(ctx.getRequest().getSession()).getFileFolderName(), getFieldValue("root"));
		filesParams.put("path", path);
		filesParams.put("webaction", "changeRenderer");
		filesParams.put("page", "meta");
		String backURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "content");
		backURL = URLHelper.addParam(backURL, "comp_id", "cp_" + getId());
		backURL = URLHelper.addParam(backURL, "webaction", "editPreview");
		if (ctx.isEditPreview()) {
			backURL = URLHelper.addParam(backURL, "previewEdit", ctx.getRequest().getParameter("previewEdit"));
		}
		filesParams.put(ElementaryURLHelper.BACK_PARAM_NAME, backURL);
		String staticLinkURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		String linkToResources = "<a class=\"browse-link btn btn-default btn-xs\" href=\"" + staticLinkURL + "\">" + i18nAccess.getText("content.goto-static") + "</a>";

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputName("root") + "\">root</label>");
		FileFilter filter = FileFilter.getInstance(ctx);
		List<File> dirList = ResourceHelper.getAllDirList(filter.getRoot());
		dirList.add(0, filter.getRoot());
		out.println(XHTMLHelper.getInputOneSelect(createKeyWithField("root"), ResourceHelper.removePrefixFromPathList(dirList, filter.getRoot().getCanonicalPath()), getFieldValue("root"), true));
		out.println(linkToResources + "</div>");

		out.println("<div class=\"line\">");
		out.println("<div class=\"label\">tags</div>");
		List<String> tags = getTags();
		for (String tag : ctx.getGlobalContext().getTags()) {
			out.println("<input type=\"checkbox\" name=\"" + createKeyWithField("tags") + "\" id=\"" + createKeyWithField(tag) + "\" " + (tags.contains(tag) ? " checked=\"checked\" " : "") + " value=\"" + tag + "\" />");
			out.println("<label class=\"radio\" for=\"" + createKeyWithField(tag) + "\">" + tag + "</label>");
		}
		out.println("</div>");
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + createKeyWithField("ext") + "\">accepted files</label>");
		out.println("<input type=\"text\" name=\"" + createKeyWithField("ext") + "\" value=\"" + getFieldValue("ext") + "\" />");
		out.println("</div>");

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + createKeyWithField("noext") + "\">refused files</label>");
		out.println("<input type=\"text\" name=\"" + createKeyWithField("noext") + "\" value=\"" + getFieldValue("noext") + "\" />");
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}

	@Override
	public String performUpload(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		Collection<FileItem> items = requestService.getAllFileItem();
		for (FileItem item : items) {
			File file = new File(item.getName());
			FileFilter filter = FileFilter.getInstance(ctx);
			filter.setRoot(new File(URLHelper.mergePath(filter.getRoot().getCanonicalPath(), getFieldValue("root"))));
			File targetFile = new File(URLHelper.mergePath(filter.getRoot().getAbsolutePath(), StringHelper.createFileName(file.getName())));
			targetFile = ResourceHelper.getFreeFileName(targetFile);
			InputStream in = item.getInputStream();
			try {
				ResourceHelper.writeStreamToFile(in, targetFile);
			} finally {
				ResourceHelper.closeResource(in);
			}
		}
		return null;
	}

	@Override
	public boolean isUploadOnDrop() {
		return true;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}

}
