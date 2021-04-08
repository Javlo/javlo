package org.javlo.component.list;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.service.ReverseLinkService;
import org.javlo.utils.ConfigurationProperties;

/**
 * @author bdumont
 */
public class FAQComponent extends AbstractVisualComponent {

	public static abstract class FAQItem {

		abstract public void appendLine(String line);

		public String getType() {
			return getClass().getSimpleName().toLowerCase();
		}

	}

	public static class Question extends FAQItem {

		private String question;
		private String answer;

		public Question(String question) {
			this(question, null);
		}

		public Question(String question, String answer) {
			super();
			this.question = question;
			this.answer = answer;
		}

		@Override
		public void appendLine(String line) {
			if (answer != null) {
				answer += "\n" + line;
			} else if (question != null) {
				question += "\n" + line;
			}
		}

		public String getAnswer() {
			return answer;
		}

		public String getQuestion() {
			return question;
		}

		public void setAnswer(String answer) {
			this.answer = answer;
		}

		public void setQuestion(String question) {
			this.question = question;
		}

	}

	public static class Title extends FAQItem {
		private String text;

		public Title(String text) {
			super();
			this.text = text;
		}

		@Override
		public void appendLine(String line) {
			text += "\n" + line;
		}

		public String getText() {
			return text;
		}

		public void setText(String question) {
			text = question;
		}
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		if (getValue().contains("faq.")) { // Old style: remove this alternative if no more needed
			ConfigurationProperties properties = new ConfigurationProperties();
			try {
				properties.load(stringToStream(getValue()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			properties.setEncoding(ContentContext.CHARACTER_ENCODING);
			String title = properties.getString("faq.title");

			List<FAQItem> items = new LinkedList<FAQItem>();
			for (int i = 0; i < 99999; i++) {
				String question = properties.getString("faq." + i + ".question");
				if (question != null) {
					String answer = properties.getString("faq." + i + ".answer");
					items.add(new Question(question, answer));
				}
			}

			/* transform old value to new value */

			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println("t:" + title);
			for (FAQItem faqItem : items) {
				if (faqItem instanceof Question) {
					out.println("q:" + ((Question) faqItem).question);
					out.println("a:" + ((Question) faqItem).answer);
				}
			}
			out.close();
			String newValue = new String(outStream.toByteArray());
			setValue(newValue);
			System.out.println("***** NEW VALUE ADDED : ");
			System.out.println(newValue);
			System.out.println("");
			setModify();

		}
		return super.getEditXHTMLCode(ctx);
	}

	@Override
	public String getHexColor() {
		return WEB2_COLOR;
	}

	@Override
	public String getType() {
		return "faq";
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		List<FAQItem> items = new LinkedList<FAQItem>();
		Title firstTitle = null;
		
		ReverseLinkService rls = ReverseLinkService.getInstance(ctx.getGlobalContext());
		
		String[] lines = getValue().split("\\r\\n|\\n|\\r");
		FAQItem lastItem = null;
		for (String line : lines) {
			if (line.startsWith(".")) {
				// Skip hidden line
			} else if (line.startsWith("t:") || line.startsWith("T:")) {
				Title title = new Title(line.substring(2));
				lastItem = title;
				items.add(lastItem);
				if (firstTitle == null) {
					firstTitle = title;
				}
			} else if (line.startsWith("q:") || line.startsWith("Q:")) {
				lastItem = new Question(line.substring(2));
				items.add(lastItem);
			} else if (line.startsWith("a:") || line.startsWith("A:")) {
				if (lastItem instanceof Question) {
					Question question = (Question) lastItem;
					String answer = line.substring(2);
					answer = rls.replaceLink(ctx, this, answer);
					answer = XHTMLHelper.autoLink(answer);
					question.setAnswer(answer);
				}
			} else if (lastItem != null) {
				lastItem.appendLine(line);
			}
		}
		
		ctx.getRequest().setAttribute("firstTitle", firstTitle);
		ctx.getRequest().setAttribute("items", items);
	}

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return !StringHelper.isEmpty(getValue());
	}

}
