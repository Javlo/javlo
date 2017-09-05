package org.javlo.comparator;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.javlo.context.GlobalContext;

public class LanguageListSorter {
	
	public static interface ILanguage {
		public String getSortLanguage();
	}
	
	public static List<? extends ILanguage> sort(GlobalContext globalContext, List<? extends ILanguage> list) {
		final List<String> languages = new LinkedList<String>(globalContext.getContentLanguages());
		
		Collections.sort(list, new Comparator<ILanguage>() {
			@Override
			public int compare(ILanguage o1, ILanguage o2) {				
				return languages.indexOf(o1.getSortLanguage())-languages.indexOf(o2.getSortLanguage());
			}
			
		});
		
		return list;
	}
}
