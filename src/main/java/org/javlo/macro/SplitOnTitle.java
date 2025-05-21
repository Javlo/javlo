package org.javlo.macro;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.text.WysiwygParagraph;
import org.javlo.component.title.Heading;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SplitOnTitle extends AbstractMacro {

	private String splitTag = "h2";

	public SplitOnTitle (String tag) {
		this.splitTag = tag;
	}

	@Override
	public String getName() {
		return "split-on-title-"+splitTag;
	}

	protected String getSplitTag() {
		return splitTag;
	}

	protected static Integer getTitleLevel(String tag) {
		return StringHelper.extractNumber(tag);
	}

	protected static boolean isTitle(String tag) {
        return tag.length() == 2 && tag.toLowerCase().charAt(0) == 'h';
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		ContentService contentService = ContentService.getInstance(ctx.getRequest());
		MenuElement currentPage = ctx.getCurrentPage();

		ContentContext mainAreaCtx = ctx.getContextWithArea(ComponentBean.DEFAULT_AREA);

		ContentElementList comps = currentPage.getContent(mainAreaCtx);
		int countChange = 0;
		String msg = "no '"+getSplitTag()+"' found on this page.";
		while (comps.hasNext(mainAreaCtx)) {

			IContentVisualComponent comp = comps.next(mainAreaCtx);

			if (comp.getType().equals(WysiwygParagraph.TYPE)) {

				WysiwygParagraph paragraph = (WysiwygParagraph) comp;
				String html = paragraph.getValue();

				Document doc = Jsoup.parseBodyFragment(html);
				Elements elements = doc.body().children();

				List<String> splitTags = new ArrayList<>();
				List<String> splitContents = new ArrayList<>();

				String splitTagName = getSplitTag();
				Element currentTag = null;
				StringBuilder currentContent = new StringBuilder();

				for (Element el : elements) {
					if (el.tagName().equalsIgnoreCase(splitTagName)) {
						if (currentTag != null) {
							splitTags.add(currentTag.outerHtml());
							splitContents.add(currentContent.toString());
							currentContent.setLength(0); // Reset buffer
						}
						currentTag = el;
					} else {
						if (currentTag != null) {
							currentContent.append(el.outerHtml());
						}
					}
				}

				// Add the last segment if necessary
				if (currentTag != null) {
					splitTags.add(currentTag.outerHtml());
					splitContents.add(currentContent.toString());
				}

				boolean isTitle = isTitle(splitTagName);
				Integer titleDepth = getTitleLevel(splitTagName);
				MenuElement page = ctx.getCurrentPage();

				String latestId = comp.getId();

				for (int i = 0; i < splitTags.size(); i++) {
					countChange++;
					msg = "";
					ComponentBean newComp = new ComponentBean();
					if (isTitle) {
						newComp.setType(Heading.TYPE);
						String text = Jsoup.parse(splitTags.get(i)).body().child(0).text();
						newComp.setValue("depth="+titleDepth+"\ntext="+text);
					} else {
						newComp.setType(WysiwygParagraph.TYPE);
						newComp.setValue(splitTags.get(i));
					}
					latestId = contentService.createContent(mainAreaCtx, newComp, latestId, false);
					newComp = new ComponentBean();

					newComp.setType(WysiwygParagraph.TYPE);
					newComp.setValue(splitContents.get(i));
					latestId = contentService.createContent(mainAreaCtx, newComp, latestId, false);

					System.out.println("Balise : " + splitTags.get(i));
					System.out.println("Contenu : " + splitContents.get(i));
				}
			}
			currentPage.removeContent(ctx, comp.getId(), true);
			PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
		}

		if (msg.length() == 0) {
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage("split on " + countChange + " tags.", GenericMessage.INFO));
		}

		return msg;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

	@Override
	public String getIcon() {
		return "bi bi-signpost-split";
	}
};
