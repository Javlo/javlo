package org.javlo.client.localmodule.service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.javlo.client.localmodule.model.ServerConfig;
import org.javlo.helper.URLHelper;

public class IMClientService {
	private static final Logger logger = Logger.getLogger(IMClientService.class.getName());

	private static final long WAIT_TIME = 10 * 1000; //10 secondes

	private static IMClientService instance;
	public static IMClientService getInstance() {
		synchronized (IMClientService.class) {
			if (instance == null) {
				instance = new IMClientService();
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

	private IMClientService() {
	}

	public void start() {
		synchronized (lock) {
			if (!isStarted()) {
				stopping = false;
				synchroThread = new Thread(IMClientService.class.getSimpleName()) {
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
					ServerConfig[] servers;
					synchronized (factory.getConfig().lock) {
						servers = factory.getConfig().getServers();
					}
					for (ServerConfig server : servers) {
						HttpClient httpClient = factory.getRawHttpClient();
						refreshIMStatus(server, httpClient);
					}
					//Wait next loop
					logger.info("- wait ----------------------------------------------------------");
					waiting = true;
					lock.wait(WAIT_TIME);
					waiting = false;
				}
			}
		} catch (Exception ex) {
			factory.getTray().displayErrorMessage(factory.getI18n().get("error.synchro.fatal"), ex, false);
			logger.log(Level.SEVERE, ex.getClass().getSimpleName() + " occured during synchro process", ex);
		}
	}

	private void refreshIMStatus(ServerConfig server, HttpClient httpClient) {
		try {
			String url = server.getServerURL() + "&webaction=communication.RefreshAIM";
			url = URLHelper.changeMode(url, "ajax");
			System.out.println(url);
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpget);
			HttpEntity entity = response.getEntity();

			System.out.println(response.getStatusLine());
			if (entity != null) {
				System.out.println("Response content length: " + entity.getContentLength());
			}
			System.out.println(EntityUtils.toString(entity));
		} catch (ClientProtocolException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}

}
