package org.javlo.client.localmodule.service.synchro;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.javlo.client.localmodule.model.ServerConfig;
import org.javlo.client.localmodule.service.ServiceFactory;

public class SynchroControlService {
	private static final Logger logger = Logger.getLogger(SynchroControlService.class.getName());

	private static final long WAIT_TIME = 10 * 60 * 1000; //10 minutes

	private static SynchroControlService instance;
	public static SynchroControlService getInstance() {
		synchronized (SynchroControlService.class) {
			if (instance == null) {
				instance = new SynchroControlService();
			}
			return instance;
		}
	}

	private ServiceFactory factory = ServiceFactory.getInstance();

	private final Object lock = new Object();
	private Thread synchroThread = null;
	private boolean stopping = false;

	private String lastKey = null;
	private ObserverSynchroService ss = null;

	private boolean waiting = false;

	private SynchroControlService() {
	}

	public void start() {
		synchronized (lock) {
			if (!isStarted()) {
				stopping = false;
				synchroThread = new Thread(SynchroControlService.class.getSimpleName()) {
					@Override
					public void run() {
						try {
							work();
						} finally {
							synchronized (lock) {
								synchroThread = null;
								stopping = false;
							}
						}
					}
				};
				synchroThread.start();
			}
		}
	}

	public boolean isStarted() {
		synchronized (lock) {
			return synchroThread != null;
		}
	}

	public boolean isWaiting() {
		return waiting;
	}

	public void wakeUp() {
		if (isWaiting()) {
			synchronized (lock) {
				lock.notify();
			}
		}
	}

	public void stop() {
		synchronized (lock) {
			if (isStarted() && !stopping) {
				stopping = true;
				lock.notify();
			}
		}
	}

	private void work() {
		try {
			synchronized (lock) {
				while (!stopping) {
					//TODO synchronize/lock on config?
					//TODO validate config
					//TODO make it multi server

					for (ServerConfig server : factory.getConfig().getBean().getServers()) {
						//Get the service
						File localFolder = server.getSynchronizedFolderFile();
						if (localFolder == null) {
							continue;
						}
						String key = server.getServerURL() + "|" + localFolder.getAbsolutePath();

						if (ss == null || lastKey == null || !key.equals(lastKey)) {
							//TODO validate config
//							if (!factory.getConfig().isValid()) {
//								throw new IllegalArgumentException();
//							}
							ss = ObserverSynchroService.createInstance(new TokenHttpClientService(factory.getClient(server)), localFolder);
							lastKey = key;
						}
						//Use the service
						ss.setLocalName(factory.getConfig().getBean().getComputerName());
						factory.getTray().onSyncroStateChange(true);
						ss.synchronize();
						factory.getTray().onSyncroStateChange(false);
					}
					//Wait next loop
					logger.info("- wait ----------------------------------------------------------");
					waiting = true;
					lock.wait(WAIT_TIME);
					waiting = false;
				}
			}
		} catch (IllegalArgumentException ex) {
			factory.getTray().displayErrorMessage(factory.getI18n().get("error.synchro.config"), ex, false);
		} catch (InterruptedException ex) {
			//Going out
		} catch (Exception ex) {
			factory.getTray().displayErrorMessage(factory.getI18n().get("error.synchro.fatal"), ex, false);
			logger.log(Level.SEVERE, ex.getClass().getSimpleName() + " occured during synchro process", ex);
		} finally {
			factory.getTray().onSyncroStateChange(false);
		}
	}
}
