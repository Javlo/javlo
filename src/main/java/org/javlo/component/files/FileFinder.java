package org.javlo.component.files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.module.file.FileBean;
import org.javlo.user.UserSecurity;
import org.javlo.ztatic.StaticInfo;

public class FileFinder extends AbstractPropertiesComponent {

	private static final String TYPE = "file-finder";

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

	public static List<FileBean> getFileList(ContentContext ctx, FileFilter filter) throws Exception {
		List<FileBean> outFileList = new LinkedList<FileBean>();
		for (File file : ResourceHelper.getAllFiles(filter.getRoot(), null)) {
			StaticInfo info = StaticInfo.getInstance(ctx, file);
			if (filter.match(ctx, info)) {
				outFileList.add(new FileBean(ctx, info));
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
		filter.setExt(StringHelper.stringToCollection(getFieldValue("ext"),","));
		filter.setNoext(StringHelper.stringToCollection(getFieldValue("noext"),","));
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
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputName("root") + "\">root</label>");
		FileFilter filter = FileFilter.getInstance(ctx);
		List<File> dirList = ResourceHelper.getAllDirList(filter.getRoot());
		dirList.add(0, filter.getRoot());
		out.println(XHTMLHelper.getInputOneSelect(createKeyWithField("root"), ResourceHelper.removePrefixFromPathList(dirList, filter.getRoot().getCanonicalPath()), getFieldValue("root"), true));
		out.println("</div>");
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

}
