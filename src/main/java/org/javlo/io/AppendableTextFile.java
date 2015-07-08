package org.javlo.io;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class AppendableTextFile implements Closeable {
	
	private File file;
	private PrintWriter writer;

	public AppendableTextFile(File file) throws IOException {		
		this.file = file;
	}
	
	protected PrintWriter getWriter() throws IOException {
		if (writer != null) {
			if (writer.checkError()) {
				close();
				writer = null;
			}
		}
		if (writer == null) {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
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
		getWriter().println(line);
	}
	
	public synchronized void print(String line) throws IOException {
		getWriter().print(line);
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

}
