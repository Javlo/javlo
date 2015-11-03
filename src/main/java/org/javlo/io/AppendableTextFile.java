package org.javlo.io;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.javlo.context.ContentContext;

public class AppendableTextFile implements Closeable {
	
	private File file;
	private BufferedWriter writer;
	private boolean autoFlush = false;

	public AppendableTextFile(File file) throws IOException {		
		this.file = file;
	}
	
	protected BufferedWriter getWriter() throws IOException {
		if (writer == null) {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), ContentContext.CHARACTER_ENCODING));
		}
		return writer;
	}

	@Override
	public void close() throws IOException {
		if (writer != null) {
			writer.close();
		}
	}
	
	@Override
	protected void finalize() throws Throwable {	
		super.finalize();
		close();
	}
	
	public synchronized void println(String line) throws IOException {		
		getWriter().append(line);
		getWriter().newLine();
		if (autoFlush) {
			getWriter().flush();
		}
	}
	
	
	public synchronized void print(String line) throws IOException {
		getWriter().append(line);
		if (autoFlush) {
			getWriter().flush();
		}
	}
	
	public static void main(String[] args) throws IOException {
		File file = new File("c:/trans/test.properties");
		AppendableTextFile append = new AppendableTextFile(file);
		append.println("key1=value1");
		append.println("key2=value2");
		append.close();
		append = new AppendableTextFile(file);
		append.println("key3=value3");
		append.println("key4=value4");
		append.close();
	}
	
	public File getFile() {
		return file;
	}

	public boolean isAutoFlush() {
		return autoFlush;
	}

	public void setAutoFlush(boolean autoFlush) {
		this.autoFlush = autoFlush;
	}

}
