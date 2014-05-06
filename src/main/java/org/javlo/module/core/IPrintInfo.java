package org.javlo.module.core;

import java.io.PrintStream;

import org.javlo.context.ContentContext;

/**
 * print info over a instance of the class. (debug)
 * @author pvandermaesen
 *
 */
public interface IPrintInfo {

	public void printInfo(ContentContext ctx, PrintStream out);
}
