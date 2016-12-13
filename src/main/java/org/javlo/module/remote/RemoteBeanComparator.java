package org.javlo.module.remote;

import java.util.Comparator;

public class RemoteBeanComparator implements Comparator<RemoteBean> {

	public RemoteBeanComparator() {
	}

	@Override
	public int compare(RemoteBean o1, RemoteBean o2) {		
		return o2.getSiteCharge()-o1.getSiteCharge();
	}

}
