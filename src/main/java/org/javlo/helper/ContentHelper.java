package org.javlo.helper;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.text.Paragraph;
import org.javlo.component.title.SubTitle;
import org.javlo.component.title.Title;
import org.javlo.helper.XMLManipulationHelper.BadXMLException;
import org.javlo.helper.XMLManipulationHelper.TagDescription;

public class ContentHelper {

	/**
	 * remove tag. sample: <a href="#">link</a> -> link
	 * 
	 * @param text
	 *            XHTML Code
	 * @return simple text
	 */
	public static String removeTag(String text) {
		StringBuffer notTagStr = new StringBuffer();
		boolean inTag = false;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if ((!inTag) && (c == '<')) {
				inTag = true;
			} else if (inTag && (c == '>')) {
				inTag = false;
			} else if (!inTag) {
				notTagStr.append(c);
			}
		}
		return notTagStr.toString();
	}

	public static List<ComponentBean> createContentWithHTML(String html, String lg) throws BadXMLException {
		List<ComponentBean> outContent = new LinkedList<ComponentBean>();
		TagDescription[] tags = XMLManipulationHelper.searchAllTag(html, false);
		for (TagDescription tag : tags) {
			ComponentBean newBean = null;
			if (tag.getName().equalsIgnoreCase("h1")) {
				String content = removeTag(tag.getInside(html));
				newBean = new ComponentBean(Title.TYPE, content, lg);
			} else if (tag.getName().equalsIgnoreCase("p")) {
				String content = removeTag(tag.getInside(html));
				newBean = new ComponentBean(Paragraph.TYPE, content, lg);
			} else {
				for (int i = 2; i < 8; i++) {
					if (tag.getName().equalsIgnoreCase("h" + i)) {
						String content = removeTag(tag.getInside(html));
						newBean = new ComponentBean(SubTitle.TYPE, content, lg);
						newBean.setStyle("" + i);
					}
				}
			}
			if (newBean != null) {
				newBean.setValue(newBean.getValue().replace("&nbsp;", "").trim());
				if (newBean.getValue().length() > 0) {
					outContent.add(newBean);
				}
			}
		}
		return outContent;
	}

	public static void main(String[] args) {
		try {
			String html = ResourceHelper.loadStringFromFile(new File("d:/trans/test_doc.htm"));
			List<ComponentBean> content = createContentWithHTML(html, "en");
			for (ComponentBean componentBean : content) {
				System.out.println("**** " + componentBean.getType()); // TODO: remove debug trace
				System.out.println(componentBean.getValue()); // TODO: remove debug trace
				System.out.println("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
