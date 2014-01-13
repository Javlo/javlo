package org.javlo.component.web2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

public class Survey extends AbstractVisualComponent implements IAction {
	
	private Properties bundle;
	
	public static class Conclusion {
		private String text;
		private String link;		
		public Conclusion(String text, String link) {
			super();
			this.text = text;
			this.link = link;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public String getLink() {
			return link;
		}
		public void setLink(String link) {
			if (link != null && link.trim().length() > 0) {
				this.link = link;
			} else {
				this.link = null;
			}
		}
		
	}
	
	public static class Question {
		private String question;
		private List<String> responses;
		private List<Conclusion> conclusions;
		private List<Integer> visitorResponses = new LinkedList<Integer>();
		private boolean done;		
		
		public Question(String question, List<String> responses, List<Conclusion> conclusions) {
			super();
			this.question = question;
			this.responses = responses;
			this.conclusions = conclusions;
		}
		public String getQuestion() {
			return question;
		}
		public void setQuestion(String question) {
			this.question = question;
		}
		public List<String> getResponses() {
			return responses;
		}
		public void setResponses(List<String> responses) {
			this.responses = responses;
		}
		public List<Conclusion> getConclusions() {
			return conclusions;
		}
		public void setConclusion(List<Conclusion> conclusions) {
			this.conclusions = conclusions;
		}
		public boolean isDone() {
			return done;
		}
		public void setDone(boolean done) {
			this.done = done;
		}
		public void addVisitorResponse(Integer responseKey) {
			visitorResponses.add(responseKey);
		}
		public Conclusion getConclusion() {
			if (!done) {
				return null;
			} else {
				if (conclusions.size() == 0) {
					return new Conclusion("error, conclusion not found.",null);
				}
				int concIndex = visitorResponses.size();
				while (StringHelper.isEmpty(conclusions.get(concIndex)) && concIndex < conclusions.size()) {
					concIndex++;
				}
				if (StringHelper.isEmpty(conclusions.get(concIndex))) {
					while (StringHelper.isEmpty(conclusions.get(concIndex))) {
						concIndex--;
					}
				}
				return conclusions.get(concIndex);
			}
		}
	}
	
	public static final String TYPE = "survey";
	
	@Override
	protected void init() throws ResourceNotFoundException {	
		super.init();
		if (getValue().length() == 0) {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println("label.legend=Survey");
			out.println("label.submit=ok");
			out.println("");
			out.println("Q1=");
			out.println("Q1.R1=");
			out.println("Q1.R2=");
			out.println("Q1.R3=");
			out.println("Q1.R4=");
			out.println("Q1.R5=");
			out.println("Q1.C1=");
			out.println("Q1.C1.link=");
			out.println("Q1.C2=");
			out.println("Q1.C2.link=");
			out.println("Q1.C3=");
			out.println("Q1.C4=");
			out.println("Q1.C5=");
			out.close();
			setValue(new String(outStream.toByteArray()));
		}
	}
	
	protected List<Question> getQuestions(ContentContext ctx) throws Exception {
		 List<Question> outQuestions = new LinkedList<Survey.Question>();
		Properties prop = new Properties();
		try {
			prop.load(new StringReader(getValue()));
			for (int q=0; q<1000; q++) {
				if (prop.getProperty("Q"+q) != null) {
					List<String> responses = new LinkedList<String>();					
					int r=1;
					String rep = prop.getProperty("Q"+q+".R"+r); 
					while (rep != null) {
						responses.add(rep);
						r++;
						rep = prop.getProperty("Q"+q+".R"+r);
					}					
					int c=0;
					String conc = prop.getProperty("Q"+q+".C"+c);
					List<Conclusion> conclusions = new LinkedList<Conclusion>();
					while (conc != null) {
						conclusions.add(new Conclusion(conc, URLHelper.smartLink(ctx, prop.getProperty("Q"+q+".C"+c+".link") ) ) );
						c++;
						conc = prop.getProperty("Q"+q+".C"+c);
					}
					outQuestions.add(new Question(prop.getProperty("Q"+q), responses, conclusions));
				}				
			}
			return outQuestions;
		} catch (IOException e) {
			e.printStackTrace();
			return Collections.EMPTY_LIST;
		}
		
	}
	
	public Properties getLocalConfig(boolean reload) {
		if (bundle == null || reload) {
			bundle = new Properties();
			try {
				bundle.load(new StringReader(getValue()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bundle;
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("ci18n", getLocalConfig(false));
		// don't erase the action result.
		if (ctx.getRequest().getAttribute("questions") == null) {
			ctx.getRequest().setAttribute("questions", getQuestions(ctx));
		}
	}

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public boolean isUnique() {	
		return true;
	}

	@Override
	public String getActionGroupName() {
		return TYPE;
	}
	
	public static String performSubmit(RequestService rs, ContentService content, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		Survey survey = (Survey)content.getComponent(ctx, rs.getParameter("compid", null));
		if (survey == null) {
			return "technical error, survey not found.";
		} else {
			List<Question> questions = survey.getQuestions(ctx);
			int q=0;
			for (Question question : questions) {
				question.setDone(true);
				int r=0;
				for (String response : question.getResponses()) {
					if (rs.getParameter("q"+q+"r"+r, null) != null) {
						question.addVisitorResponse(r);						
					}
					r++;
				}
				q++;
			}
			ctx.getRequest().setAttribute("questions", questions);
		}
		return null;
	}

}
