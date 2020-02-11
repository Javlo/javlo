package org.javlo.component.web2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.javlo.helper.StringHelper;

public class QuizzPartyContext {

	public static final int YES = 1;
	public static final int NO = 0;
	public static final int NA = Integer.MAX_VALUE;

	public static class Response {
		public Response(int response, String sessionId) {
			super();
			this.response = response;
			this.sessionId = sessionId;
		}

		public int response;
		public String sessionId;
	}
	
	public static class Result {
		public int yes = 0;
		public int no = 0;
		public int na = 0;
	}
	
	private WeakHashMap<String, QuizzPlayer> players = new WeakHashMap<>();

	private List<List<Response>> responses = new ArrayList<>();

	private static Logger logger = Logger.getLogger(QuizzPartyContext.class.getName());

	private static final String KEY = "quizz";
	private static final Map<Integer, QuizzPartyContext> quizzMap = new WeakHashMap<>();
	private static final int MAX_QUIZZ = 10000000;
	public static final int HASH_SIZE = 1;

	private String name = null;
	private String masterSessionId = null;
	private int participant = 0;
	private int question = 1;
	private int check = 0;
	private Result result = new Result();
	private boolean reset;

	private QuizzPartyContext(String name) {
		this.name = name;
	}

	public static final QuizzPartyContext getInstance(HttpSession session) throws Exception {
		QuizzPartyContext quizz = (QuizzPartyContext) session.getAttribute(KEY);
		if (quizz != null) {
			if (quizz.isReset()) {
				session.removeAttribute(KEY);
			}
		}
		return quizz;
	}

	public static final QuizzPartyContext getInstance(HttpSession session, String name) throws Exception {
		if (name == null) {
			synchronized (KEY) {
				int i = 1;
				for (; i < MAX_QUIZZ && quizzMap.get(i) != null; i++) {
				}
				if (i == MAX_QUIZZ) {
					throw new Exception("to many quizz try again later.");
				}
			name = StringHelper.getRandomString(HASH_SIZE, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ") + i;
				QuizzPartyContext outQuizz = new QuizzPartyContext(name);
				outQuizz.setMasterSessionId(session.getId());
				session.setAttribute(KEY, outQuizz);
				outQuizz.responses.add(new LinkedList<Response>());
				quizzMap.put(i, outQuizz);
				return outQuizz;
			}
		} else {
			name = name.toUpperCase();
			if (StringHelper.isDigit(name.substring(HASH_SIZE))) {
				int number = Integer.parseInt(name.substring(HASH_SIZE));
				QuizzPartyContext outQuizz = quizzMap.get(number);
				
				QuizzPlayer player = new QuizzPlayer(session.getId());
				session.setAttribute("player", player);
				outQuizz.players.put(player.getSessionId(), player);
				
				if (outQuizz != null) {
					if (outQuizz != null && outQuizz.getName().equals(name)) {
						session.setAttribute(KEY, outQuizz);
						return outQuizz;
					} else {
						return null;
					}
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMasterSessionId() {
		return masterSessionId;
	}

	public void setMasterSessionId(String masterSessionId) {
		this.masterSessionId = masterSessionId;
	}

	public int getParticipant() {
		return participant;
	}

	public void setParticipant(int participant) {
		this.participant = participant;
	}

	public int getQuestion() {
		return question;
	}

	public void nextQuestion() {
		this.question++;
		if (responses.size() < this.question) {
			responses.add(new LinkedList<Response>());
		}
		updateCheck();
	}

	public void previousQuestion() {
		if (this.question>1) {
			this.question--;
			updateCheck();
		}
	}

	private void updateCheck() {
		check = responses.get(this.question - 1).size();
		result = new Result();
		for (Response r : responses.get(this.question-1)) {
			if (r.response == YES) {
				result.yes++;
			} else if (r.response == NO) {
				result.no++;
			} else if (r.response == NA) {
				result.na++;
			}
		}
		int total = result.yes+result.no+result.na;
		if (total>0) {
			result.yes = Math.round(result.yes*100/total);
			result.no = Math.round(result.no*100/total);
			result.na = Math.round(result.na*100/total);
		}
	}

	public int getCheck() {
		return check;
	}

	public void setCheck(int check) {
		this.check = check;
	}

	public synchronized void vote(int vote, String sessionId) {
		Iterator<Response> responseIte = responses.get(this.question - 1).iterator();
		while (responseIte.hasNext()) {
			if (responseIte.next().sessionId.equals(sessionId)) {
				responseIte.remove();
			}
		}
		responses.get(this.question - 1).add(new Response(vote, sessionId));
		updateCheck();
	}

	public int getYES() {
		return YES;
	}

	public int getNO() {
		return NO;
	}

	public int getNA() {
		return NA;
	}
	
	public Result getResult() {
		return result;
	}
	
	public void reset(HttpSession session) {
		setReset(true);
		session.removeAttribute(KEY);
	}

	public boolean isReset() {
		return reset;
	}

	public void setReset(boolean reset) {
		this.reset = reset;
	}

	public void checkPlayers() {
		int countPlayer = 0;
		Iterator<Map.Entry<String, QuizzPlayer>> ite = players.entrySet().iterator();
		while (ite.hasNext()) {
			if (ite.next().getValue() != null) {
				countPlayer++;
			} else {
				ite.remove();
			}
		}
		participant = countPlayer;		
	}

}
