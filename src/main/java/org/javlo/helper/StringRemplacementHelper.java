package org.javlo.helper;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;


public class StringRemplacementHelper {
	
	private Collection<StringReplacement> actions = new TreeSet<StringReplacement>(new StringReplacementComparator());
	
	private class StringReplacement {
		int sourceStart;
		int sourceEnd;
		String content;
		public StringReplacement(){
			
		};
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public int getSourceEnd() {
			return sourceEnd;
		}
		public void setSourceEnd(int sourceEnd) {
			this.sourceEnd = sourceEnd;
		}
		public int getSourceStart() {
			return sourceStart;
		}
		public void setSourceStart(int sourceStart) {
			this.sourceStart = sourceStart;
		}
	}
	
	private class StringReplacementComparator implements Comparator<StringReplacement> {
		public int compare(StringReplacement arg0, StringReplacement arg1) {
			return arg1.getSourceStart()-arg0.getSourceStart();
		}		
	}
	
	/**
	 * add replacement only if there are no other replacement
	 * @param sourceStart
	 * @param sourceEnd
	 * @param content
	 * @throws InvalidParameterException
	 * @return true if replacement done.
	 */
	public boolean addReplacementIfPossible (int sourceStart, int sourceEnd, String content) throws InvalidParameterException {
		if (sourceStart < 0) {
			throw new InvalidParameterException("bad start index (<0)");
		}
		if (sourceStart > sourceEnd) {
			return false;
		}
		for (StringReplacement oldRemp : actions) { // search if remplacement in a other remplacement
			if ((sourceStart > oldRemp.getSourceStart())&&(sourceStart < oldRemp.getSourceEnd())) {
				return false;
			}
		}
		for (StringReplacement rep : actions) {
			if (rep.getSourceStart() >= sourceStart && rep.getSourceStart() <= sourceEnd) {
				return false;
			}
			if (rep.getSourceEnd() >= sourceStart && rep.getSourceEnd() <= sourceEnd) {
				return false;
			}
		}
		StringReplacement remp = new StringReplacement();	
		remp.setSourceStart(sourceStart);
		remp.setSourceEnd(sourceEnd);
		remp.setContent(content);
		actions.add(remp);
		return true;
	}
	
	public void addReplacement (int sourceStart, int sourceEnd, String content) throws InvalidParameterException {
		if (sourceStart < 0) {
			throw new InvalidParameterException("bad start index (<0)");
		}
		if (sourceStart > sourceEnd) {
			return;
		}
		for (StringReplacement oldRemp : actions) { // search if remplacement in a other remplacement
			if ((sourceStart > oldRemp.getSourceStart())&&(sourceStart < oldRemp.getSourceEnd())) {
				return;
			}
		}
		StringReplacement remp = new StringReplacement();	
		remp.setSourceStart(sourceStart);
		remp.setSourceEnd(sourceEnd);
		remp.setContent(content);
		actions.add(remp);
	}
	
	public String start(String source) {
		StringBuffer out = new StringBuffer();
		for (StringReplacement action : actions) {
			out.append(source.substring(0, action.getSourceStart()));
			out.append(action.getContent());
			out.append(source.substring(action.getSourceEnd(), source.length()));
			source = out.toString();
			out.setLength(0);
		}
		return source;
	}
	
	public static void main(String[] args) {
		String content="<div id=\"mainCol\">---- <img src=\"images/palm.jpg\" /> ---</div>";

		StringRemplacementHelper r = new StringRemplacementHelper();
		try {
			r.addReplacement(17+1, 56, "<INCLUDE>");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String result = r.start(content);
		System.out.println("test = "+content);
		System.out.println("result = "+result);
	}

}
