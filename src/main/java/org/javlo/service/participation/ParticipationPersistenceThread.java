package org.javlo.service.participation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Logger;

import org.javlo.helper.ResourceHelper;

public class ParticipationPersistenceThread extends Thread {
	
	private static Logger logger = Logger.getLogger(ParticipationPersistenceThread.class.getName());
	
	private static final long TIME_BEFORE_SAFE = 5*1000; // 5 Sec
	
	private Properties prop;
	private File file;
	private boolean threadWait = false;
	

	public ParticipationPersistenceThread(Properties prop, File file) {
		this.prop = prop;
		this.file = file;
		setName("Participation Persistence Thread");
	}

	@Override
	public void run() {
		threadWait = true;
		synchronized (prop) {
			OutputStream out = null;
			try {
				Thread.sleep(TIME_BEFORE_SAFE);
				threadWait = false;
				logger.info("store : "+file);
				out = new FileOutputStream(file);				
				prop.store(out, "");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				ResourceHelper.closeResource(out);
			}		
		}
	}

	public boolean isThreadWait() {
		return threadWait;
	}

}
