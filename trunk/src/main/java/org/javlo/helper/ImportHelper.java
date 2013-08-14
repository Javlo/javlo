package org.javlo.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.CssSelectorNodeFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.javlo.component.image.GlobalImage;
import org.javlo.component.text.WysiwygParagraph;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;

public class ImportHelper {

	public static class ContentSelector {
		private String title;
		private String image;
		private String content;
		private String dir;

		public ContentSelector(String title, String image, String content, String dir) {
			this.title = title;
			this.image = image;
			this.content = content;
			this.dir = dir;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getImage() {
			return image;
		}

		public void setImage(String image) {
			this.image = image;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getDir() {
			return dir;
		}

		public void setDir(String dir) {
			this.dir = dir;
		}

	}

	private static void getAllChildren(Node inNode, Collection<Node> children) {
		NodeList nodeList = inNode.getChildren();
		if (nodeList != null) {
			for (Node node : nodeList.toNodeArray()) {
				children.add(node);
				getAllChildren(node, children);
			}
		}
	}

	private static Collection<Node> getAllChildren(Node node) {
		Collection<Node> outNodes = new LinkedList<Node>();
		getAllChildren(node, outNodes);
		return outNodes;
	}

	public static String importHTML(ContentContext ctx, String url, ContentSelector selector) throws Exception {
		try {
			String msg = null;
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			URL pageURL = new URL(url);
			Parser htmlParser = new Parser(pageURL.openConnection());
			NodeFilter cssFilter = new CssSelectorNodeFilter(selector.getTitle().trim());
			MenuElement page = ctx.getCurrentPage();
			String latestComponentId = "0";
			// title
			NodeList nodeList = htmlParser.extractAllNodesThatMatch(cssFilter);
			if (nodeList.size() == 0) {
				msg = msg + "title not found (" + selector.getTitle() + "). ";
			} else {
				String title = nodeList.elements().nextNode().toPlainTextString();
				latestComponentId = MacroHelper.addContentIfNotExist(ctx, page, latestComponentId, Title.TYPE, title);
			}
			htmlParser.reset();
			// image
			NodeFilter imageFilter = new CssSelectorNodeFilter(selector.getImage().trim());
			nodeList = htmlParser.extractAllNodesThatMatch(imageFilter);
			Node imageNode = null;
			if (nodeList.size() == 0) {
				msg = msg + "Content not found (" + selector.getContent() + "). ";
			} else {
				imageNode = nodeList.elements().nextNode();
				if (imageNode instanceof TagNode) {
					TagNode tag = (TagNode) imageNode;
					URL imageURL = null;
					String imageTitle = null;
					if (tag.getTagName().equalsIgnoreCase("a")) { // a tag
						imageURL = new URL(tag.getAttribute("href"));
						imageTitle = tag.getAttribute("title");
					} else if (tag.getTagName().equalsIgnoreCase("img")) { // img tag
						imageURL = new URL(tag.getAttribute("src"));
						imageTitle = tag.getAttribute("alt");
						if (imageTitle == null || imageTitle.trim().length() == 0) {
							imageTitle = tag.getAttribute("title");
						}
					}
					if (imageURL != null) {
						String remoteFileName = StringHelper.getFileNameFromPath(imageURL.getFile());
						String fileName = URLHelper.extractFileName(remoteFileName);
						File folder = new File(URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getImageFolder(), selector.getDir()));
						folder.mkdirs();
						File localFile = new File(URLHelper.mergePath(folder.getAbsolutePath(), fileName));
						if (!localFile.exists()) {
							localFile.createNewFile();
							InputStream inImage = imageURL.openStream();
							ResourceHelper.writeStreamToFile(inImage, localFile);
							inImage.close();
						}
						latestComponentId = MacroHelper.addContentIfNotExist(ctx, page, latestComponentId, GlobalImage.TYPE, "file-name=" + remoteFileName + "\ndir=" + selector.getDir() + "\nlabel=" + imageTitle, "image-center");
					}
				}
			}
			htmlParser.reset();
			// content
			NodeFilter contentFilter = new CssSelectorNodeFilter(selector.getContent().trim());
			nodeList = htmlParser.extractAllNodesThatMatch(contentFilter);
			if (nodeList.size() == 0) {
				msg = msg + "Content not found (" + selector.getContent() + "). ";
			} else {
				for (Node parentNode : nodeList.toNodeArray()) {
					for (Node node : getAllChildren(parentNode)) {
						if (node.getStartPosition() == imageNode.getStartPosition()) {
							node.getParent().getChildren().remove(node);
						}
					}
				}

				String content = nodeList.elements().nextNode().toHtml();
				latestComponentId = MacroHelper.addContentIfNotExist(ctx, page, latestComponentId, WysiwygParagraph.TYPE, content);
			}
			htmlParser.reset();

		} catch (MalformedURLException e) {
			return "url is'nt valid : " + e.getMessage();
		}
		return null;
	}

	public static void main(String[] args) {
		File file = new File("d:/trans/test_doc.doc");
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			POIFSFileSystem fileSystem = new POIFSFileSystem(fis);
			// Firstly, get an extractor for the Workbook
			POIOLE2TextExtractor oleTextExtractor = ExtractorFactory.createExtractor(fileSystem);

			System.out.println("t:" + oleTextExtractor.getText());

			POITextExtractor[] embeddedExtractors = ExtractorFactory.getEmbededDocsTextExtractors(oleTextExtractor);
			System.out.println("***** ImportHelper.main : size : " + embeddedExtractors.length); // TODO: remove debug trace
			for (POITextExtractor textExtractor : embeddedExtractors) {
				if (textExtractor instanceof WordExtractor) {
					WordExtractor wordExtractor = (WordExtractor) textExtractor;
					String[] paragraphText = wordExtractor.getParagraphText();
					for (String paragraph : paragraphText) {
						System.out.println(paragraph);
					}
					// Display the document's header and footer text
					System.out.println("Footer text: " + wordExtractor.getFooterText());
					System.out.println("Header text: " + wordExtractor.getHeaderText());
				} else {
					System.out.println("not word document : " + textExtractor);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
