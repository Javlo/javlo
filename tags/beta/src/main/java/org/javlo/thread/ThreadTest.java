package org.javlo.thread;

import java.util.Date;

import org.javlo.helper.StringHelper;


public class ThreadTest extends AbstractThread {

	@Override
	public void run() {
		System.out.println("**********************************");
		System.out.println("** thread run on : "+StringHelper.renderTime(new Date()));
		System.out.println("**********************************");
	}

}
