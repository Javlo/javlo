package org.javlo.navigation;

import java.util.Comparator;

import org.javlo.component.links.SmartPageBean;

public class ReactionSmartPageBeanComparator implements Comparator<SmartPageBean> {

	private boolean reverse = false;	

	public ReactionSmartPageBeanComparator(boolean reverse) {
		this.reverse = reverse;		
	}

	@Override
	public int compare(SmartPageBean o1, SmartPageBean o2) {
		try {
			if (o1.getToTheTopLevel() != o2.getToTheTopLevel()) {
				return o2.getToTheTopLevel()-o1.getToTheTopLevel();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			if (!reverse) {
				return o1.getReactionSize() - o2.getReactionSize();
			} else {
				return o2.getReactionSize() - o1.getReactionSize();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}

}
