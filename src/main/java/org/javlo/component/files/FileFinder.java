package org.javlo.component.files;

import org.apache.commons.fileupload.FileItem;
import org.javlo.component.core.IUploadResource;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.data.taxonomy.ITaxonomyContainer;
import org.javlo.data.taxonomy.TaxonomyBean;
import org.javlo.data.taxonomy.TaxonomyDisplayBean;
import org.javlo.data.taxonomy.TaxonomyService;
import org.javlo.helper.*;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.file.FileAction;
import org.javlo.module.file.FileBean;
import org.javlo.service.RequestService;
import org.javlo.user.UserSecurity;
import org.javlo.ztatic.StaticInfo;
import org.javlo.ztatic.StaticInfoBean;

import java.io.*;
import java.util.*;

public class FileFinder extends AbstractPropertiesComponent implements IUploadResource {

	public static final String TYPE = "file-finder";

	private static String SORT_WEIGHT = "sort_weight";
	private static String SORT_NAME = "sort_name";
	private static String SORT_TITLE = "sort_title";
	private static String SORT_DATE = "sort_creation_date";
	private static String SORT_MODIFDATE = "sort_date";
	private static String SORT_NAME_DESC = "sort_name_desc";
	private static String SORT_TITLE_DESC = "sort_title_desc";
	private static String SORT_DATE_DESC = "sort_creation_date_desc";
	private static String SORT_MODIFDATE_DESC = "sort_date_desc";

	@Override
	public void prepareEdit(ContentContext ctx) throws Exception {
		super.prepareEdit(ctx);
	}

	private static String[] styleList = new String[] { SORT_WEIGHT, SORT_NAME, SORT_TITLE, SORT_DATE, SORT_MODIFDATE, SORT_NAME_DESC, SORT_TITLE_DESC, SORT_DATE_DESC, SORT_MODIFDATE_DESC };

	public static class FileFilter implements ITaxonomyContainer {
		private String text = null;
		private List<String> ext = Collections.emptyList();
		private List<String> noext = Collections.emptyList();
		private List<String> tags = Collections.emptyList();
		private Set<String> taxonomy = Collections.emptySet();
		private File root;
		private boolean acceptDirectory = false;

		public boolean isActive() {
			return !StringHelper.isEmpty(text) || taxonomy.size() > 0;
		}

		public int match(ContentContext ctx, StaticInfo file) throws Exception {
			int matchScore = 0;
			if (file == null || file.getFile() == null) {
				return 0;
			} else {
				
				if (this.getTaxonomy().size() > 0 && file.getTaxonomy(ctx).size() == 0) {
					return 0;
				}
				
				TaxonomyService taxonomyService = TaxonomyService.getInstance(ctx);
				if (acceptDirectory || !file.getFile().isDirectory()) {
					if (UserSecurity.isCurrentUserCanRead(ctx, file) && file.isShared(ctx)) {
						try {
							String fileExt = StringHelper.getFileExtension(file.getFile().getName());
							if (ext.size() == 0 || ext.contains(fileExt)) {
								if (noext.size() == 0 || !noext.contains(fileExt)) {
									if (tags.size() == 0 || !Collections.disjoint(file.getTags(ctx), tags)) {
										if (file.getFile().getCanonicalPath().startsWith(getRoot().getCanonicalPath())) {
											if (getTaxonomy().size() > 0) {
												StaticInfoBean staticInfoBean = new StaticInfoBean(ctx, file);
												boolean allMatch = true;
												for (String taxo : this.getTaxonomy()) {
													boolean match = false;
													for (String thisTaxo : staticInfoBean.getTaxonomy()) {
														TaxonomyBean bean = taxonomyService.getTaxonomyBean(thisTaxo, true);
														if (bean == null) {
															logger.severe("bean not found : " + thisTaxo);
														}
														while (bean != null) {
															if (taxo.equals(bean.getId())) {
																match = true;
															}
															bean = bean.getParent();
														}
													}
													if (!match) {
														allMatch = false;
													}
												}
												if (!allMatch) {
													return 0;
												} else {
													matchScore++;
												}
											}
											if (!StringHelper.isEmpty(text)) {
												for (String t : text.split(" ")) {
													boolean found = false;
													if (StringHelper.containsNoCase(file.getTitle(ctx), t)) {
														matchScore = matchScore + 6;
														found = true;
													}
													if (StringHelper.containsNoCase(file.getFile().getAbsolutePath(), t)) {
														matchScore = matchScore + 4;
														found = true;
													}
													if (StringHelper.containsNoCase(file.getDescription(ctx), t)) {
														matchScore = matchScore + 2;
														found = true;
													}
													if (StringHelper.containsNoCase(file.getAuthors(ctx), t)) {
														matchScore = matchScore + 1;
														found = true;
													}
													if (StringHelper.containsNoCase(StringHelper.collectionToString(file.getKeywords(ctx)), t)) {
														matchScore = matchScore + 1;
														found = true;
													}
													if (!found) {
														matchScore = 0;
														break;
													}
												}
											}
										}
									}
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
							return 0;
						}
					}
				}
			}
			return matchScore;
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

		@Override
		public Set<String> getTaxonomy() {
			return taxonomy;
		}

		public void setTaxonomy(List<String> inTaxo) {
			taxonomy = new HashSet(inTaxo);
		}
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return styleList;
	}

	protected List<FileBean> getFileList(ContentContext ctx, FileFilter filter, int max, boolean defaultFound) throws Exception {
		Map<String, FileBean> fileWithRef = new HashMap<String, FileBean>();
		List<FileBean> outFileList = new LinkedList<FileBean>();
		// Set<String> ref = new HashSet<String>();
		boolean filterActive = filter.isActive();
		for (File file : ResourceHelper.getAllFiles(filter.getRoot(), null)) {
			if (filter.acceptDirectory || file.isFile()) {
				StaticInfo info = StaticInfo.getInstance(ctx, file);
				int matchScore = filter.match(ctx, info);
				if (matchScore > 0 || (defaultFound && !filterActive)) {
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
						fileBean.setWeight(matchScore);
						if (outFileList.size() >= max) {
							break;
						}
					}
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
			} else {
				Collections.sort(outFileList, new FileBean.FileBeanComparator(ctx, 5, false));
			}
		}

		return outFileList;
	}

	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		String root= getImportFolderPath(ctx, ctx.getCurrentPage());
		if (!root.startsWith("/")) {
			root = "/" + root;
		}
		setFieldValue("root", root);
		setFieldValue("display", "true");
		return super.initContent(ctx);
	}

	public FileFilter getFileFilterInstance(ContentContext ctx) {
		FileFilter outFilter = new FileFilter();
		outFilter.text = ctx.getRequest().getParameter("text");
		outFilter.root = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), ctx.getGlobalContext().getStaticConfig().getFileFolder()));
		Collection<String> taxonomy = getSelectedTaxonomy(ctx);
		if (taxonomy != null && taxonomy.size() > 0) {
			outFilter.taxonomy = new HashSet<String>(taxonomy);
		} else {
			outFilter.taxonomy = new HashSet<String>();
		}
		// if (outFilter.taxonomy.size() == 0) {
		// outFilter.taxonomy.addAll(rootTaxonomy);
		// }
		return outFilter;
	}

	private List<String> FIELDS = Arrays.asList(new String[] { "title", "root", "tags", "ext", "noext", "taxonomy", "display", "max" });

	public List<String> getSelectedTaxonomy(ContentContext ctx) {
		List<String> out = new LinkedList<>();
		List<String> taxonomyIds = StringHelper.stringToCollection(getFieldValue("taxonomy"), ",");
		if (taxonomyIds.size() == 1) {
			out.add(taxonomyIds.get(0));
		}
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		out.addAll(rs.getParameterListValues("taxonomy"));
		for (int i = 0; i < 10; i++) {
			out.addAll(rs.getParameterListValues("taxonomy-"+i));
		}
		/** clean double **/
		Iterator<String> ite = out.iterator();
		Set<String> idFound = new HashSet<>();
		while (ite.hasNext()) {
			String id = ite.next();
			if (idFound.contains(id)) {
				ite.remove();
			} else {
				idFound.add(id);
			}
		}
		/** clean parent unselected **/
		TaxonomyService taxonomyService = TaxonomyService.getInstance(ctx);
		ite = out.iterator();
		while (ite.hasNext()) {
			String id = ite.next();
			TaxonomyBean bean = taxonomyService.getTaxonomyBean(id, true);
			if (bean == null || (bean.getParent().getParent() != null && !out.contains(bean.getParent().getId()))) {
				ite.remove();
			} else {
				idFound.add(id);
			}
		}
		return out;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);

		List<String> taxonomyIds = StringHelper.stringToCollection(getFieldValue("taxonomy"), ",");
		FileFilter filter = getFileFilterInstance(ctx);
		filter.setTags(getTags());
		filter.setExt(StringHelper.stringToCollection(getFieldValue("ext"), ","));
		filter.setNoext(StringHelper.stringToCollection(getFieldValue("noext"), ","));
		filter.setRoot(new File(URLHelper.mergePath(filter.getRoot().getCanonicalPath(), getFieldValue("root"))));
		if (!getCurrentRenderer(ctx).contains("interactive")) {
			filter.setTaxonomy(taxonomyIds);
		}
		ctx.getRequest().setAttribute("filter", filter);
		boolean display = StringHelper.isTrue(getFieldValue("display"));
		ctx.getRequest().setAttribute("display", display);

		int maxSize = 10000;
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		if (rs.getParameter("max", "").equals("10")) {
			maxSize = 10;
		}
		if (rs.getParameter("max", "").equals("100")) {
			maxSize = 100;
		}
		if (rs.getParameter("max", "").equals("1000")) {
			maxSize = 1000;
		}
		List<FileBean> files = getFileList(ctx, filter, maxSize, display);

		Set<String> lg = new HashSet<>();
		for (FileBean file : files) {
			if (!StringHelper.isEmpty(file.getLanguage())) {
				if (!lg.contains(file.getLanguage())) {
					lg.add(file.getLanguage());
				}
			}
		}
		
		ctx.getRequest().setAttribute("taxonomySelectedIdString", StringHelper.collectionToString(getSelectedTaxonomy(ctx), ","));

		ctx.getRequest().setAttribute("languages", lg);

		if (StringHelper.isDigit(getFieldValue(ctx, "max"))) {
			int max = Integer.parseInt(getFieldValue(ctx, "max"));
			while (files.size() > max) {
				files.remove(files.size() - 1);
			}
		}

		ctx.getRequest().setAttribute("files", files);
		if (taxonomyIds.size() > 0) {
			List<TaxonomyDisplayBean> beans = new LinkedList<>();
			TaxonomyService ts = TaxonomyService.getInstance(ctx);
			for (String id : taxonomyIds) {
				TaxonomyBean b = ts.getTaxonomyBean(id, true);
				if (b != null) {
					beans.add(new TaxonomyDisplayBean(ctx, b));
				}
			}
			ctx.getRequest().setAttribute("taxonomies", beans);
		}

		List<String> taxonomySelected = getSelectedTaxonomy(ctx);
		if (taxonomySelected != null && taxonomySelected.size() > 0) {
			List<TaxonomyDisplayBean> children = new LinkedList<>();
			TaxonomyService ts = TaxonomyService.getInstance(ctx);
			for (String taxoId : taxonomySelected) {
				TaxonomyBean b = ts.getTaxonomyBean(taxoId, true);
				if (b != null) {
					List<TaxonomyDisplayBean> beans = new LinkedList<>();
					for (TaxonomyBean bean : b.getChildren()) {
						beans.add(new TaxonomyDisplayBean(ctx, bean));
					}
					children.add(new TaxonomyDisplayBean(ctx, b));
				}
			}

			Collections.sort(children, new Comparator<TaxonomyDisplayBean>() {
				@Override
				public int compare(TaxonomyDisplayBean o1, TaxonomyDisplayBean o2) {
					return o1.getDepth() - o2.getDepth();
				}

			});

			ctx.getRequest().setAttribute("taxonomySelected", children);
		}
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

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + createKeyWithField("title") + "\">title</label>");
		out.println("<input type=\"text\" name=\"" + createKeyWithField("title") + "\" value=\"" + getFieldValue("title") + "\" />");
		out.println("</div>");

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

		if (ctx.getGlobalContext().getAllTaxonomy(ctx).isActive()) {
			String taxoName = createKeyWithField("taxonomy");
			out.println("<label for=\"" + getFieldValue("taxonomy") + "\">" + i18nAccess.getText("taxonomy") + "</label>");
			out.println(ctx.getGlobalContext().getAllTaxonomy(ctx).getSelectHtml(taxoName, "form-control chosen-select", StringHelper.stringToSet(getFieldValue("taxonomy"), ","), true));
			out.println("<hr />");
		}

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputName("root") + "\">root</label>");
		FileFilter filter = getFileFilterInstance(ctx);
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
		out.println("<label for=\"" + createKeyWithField("max") + "\">max files</label>");
		out.println("<input type=\"number\" name=\"" + createKeyWithField("max") + "\" value=\"" + getFieldValue("max") + "\" />");
		out.println("</div>");

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + createKeyWithField("noext") + "\">refused files</label>");
		out.println("<input type=\"text\" name=\"" + createKeyWithField("noext") + "\" value=\"" + getFieldValue("noext") + "\" />");
		out.println("</div>");

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + createKeyWithField("display") + "\">display results by default</label>");
		out.println("<input type=\"checkbox\" name=\"" + createKeyWithField("display") + "\" value=\"true\" " + (StringHelper.isTrue(getFieldValue("display")) ? " checked=\"checked\"" : "") + "/>");
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
		if (!isUploadOnDrop()) {
			return "no upload !";
		}
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		Collection<FileItem> items = requestService.getAllFileItem();
		for (FileItem item : items) {
			File file = new File(item.getName());
			FileFilter filter = getFileFilterInstance(ctx);
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
		return false;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}

	@Override
	public String getFontAwesome() {
		return "files-o";
	}

}
