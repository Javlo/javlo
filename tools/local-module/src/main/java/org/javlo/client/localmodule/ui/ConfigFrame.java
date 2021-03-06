package org.javlo.client.localmodule.ui;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import org.javlo.client.localmodule.model.AppConfig;
import org.javlo.client.localmodule.model.ServerConfig;
import org.javlo.client.localmodule.service.ConfigService;
import org.javlo.client.localmodule.service.I18nService;
import org.javlo.client.localmodule.service.ServiceFactory;
import org.javlo.helper.StringHelper;

/**
 *
 * @author bdumont
 */
public class ConfigFrame extends javax.swing.JDialog {

	private static final long serialVersionUID = 7060664378303938141L;

	private static final Logger logger = Logger.getLogger(ConfigFrame.class.getName());

	private static ConfigFrame instance;

	private I18nService i18n = I18nService.getInstance();
	private ConfigService config = ConfigService.getInstance();

	private DefaultListModel lstServersModel;

	public ConfigFrame() {
		initComponents();
		getRootPane().setDefaultButton(btnOK);
	}

	public static void showDialog() {
		synchronized (ConfigFrame.class) {
			if (instance == null) {
				instance = new ConfigFrame();
				instance.loadConfig();
				instance.setLocationRelativeTo(null);
				instance.setModalityType(ModalityType.APPLICATION_MODAL);
				instance.setVisible(true);
			} else {
				instance.toFront();
			}
		}
	}

	public void close() {
		synchronized (ConfigFrame.class) {
			this.setVisible(false);
			this.dispose();
			instance = null;
		}
	}

	private void loadConfig() {
		lstServersModel = new DefaultListModel();
		AppConfig configBean = config.getBean();
		for (ServerConfig serverConfig : configBean.getServers()) {
			lstServersModel.addElement(serverConfig.copy());
		}
		lstServers.setModel(lstServersModel);

		txtProxyHost.setText(configBean.getProxyHost());
		if (configBean.getProxyPort() == null) {
			txtProxyPort.setText(null);
		} else {
			txtProxyPort.setText("" + configBean.getProxyPort());
		}
		txtProxyUsername.setText(configBean.getProxyUsername());
		txtProxyPassword.setText(configBean.getProxyPassword());
	}

	private boolean saveConfig() {
		//Parse an check input valuesµ
		List<ServerConfig> servers = new LinkedList<ServerConfig>();
		for (Enumeration<?> enumeration = lstServersModel.elements(); enumeration.hasMoreElements();) {
			ServerConfig server = (ServerConfig) enumeration.nextElement();
			servers.add(server);
		}

		String proxyHost = StringHelper.trimAndNullify(txtProxyHost.getText());
		String proxyPortStr = StringHelper.trimAndNullify(txtProxyPort.getText());
		Integer proxyPort = null;
		if (proxyPortStr != null) {
			try {
				proxyPort = Integer.parseInt(txtProxyPort.getText());
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, i18n.get("error.invalid-proxy-port") + '\n' + i18n.get("error.details") + ex.getLocalizedMessage(), i18n.get("error"), JOptionPane.ERROR_MESSAGE);
				txtProxyPort.requestFocus();
				return false;
			}
			if (proxyPort < 1 || proxyPort > 65535) {
				JOptionPane.showMessageDialog(this, i18n.get("error.invalid-proxy-port"), i18n.get("error"), JOptionPane.ERROR_MESSAGE);
				txtProxyPort.requestFocus();
				return false;
			}
		}
		if (proxyHost != null && proxyPort == null) {
			JOptionPane.showMessageDialog(this, i18n.get("error.empty-proxy-port"), i18n.get("error"), JOptionPane.ERROR_MESSAGE);
			txtProxyPort.requestFocus();
			return false;
		}

		//Store
		boolean saved;
		synchronized (config) {
			AppConfig configBean = config.getBean();
			configBean.setServers(servers.toArray(new ServerConfig[servers.size()]));

			configBean.setProxyHost(proxyHost);
			configBean.setProxyPort(proxyPort);
			configBean.setProxyUsername(StringHelper.trimAndNullify(txtProxyUsername.getText()));
			configBean.setProxyPassword(StringHelper.trimAndNullify(new String(txtProxyPassword.getPassword())));
			try {
				config.save();
				saved = true;
			} catch (Exception ex) {
				logger.log(Level.SEVERE, "Exception during save config.", ex);
				try {
					config.reload();
				} catch (Exception ex2) {
				}
				saved = false;
			}
		}
		if (!saved) {
			JOptionPane.showMessageDialog(this, i18n.get("error.config-op"), i18n.get("error"), JOptionPane.ERROR_MESSAGE);
		} else {
			ServiceFactory.getInstance().onConfigChange();
		}
		return saved;
	}

	public void onServerUpdate(ServerConfig serverConfig) {
		int index = lstServersModel.indexOf(serverConfig);
		if (index >= 0) {
			lstServersModel.set(index, serverConfig);
		} else {
			lstServersModel.addElement(serverConfig);
		}
		lstServers.setSelectedValue(serverConfig, true);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("all")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		pnlLocal = new javax.swing.JPanel();
		lblServers = new javax.swing.JLabel();
		sclServers = new javax.swing.JScrollPane();
		lstServers = new javax.swing.JList();
		btnAddServer = new javax.swing.JButton();
		btnEditServer = new javax.swing.JButton();
		btnRemoveServer = new javax.swing.JButton();
		pnlProxy = new javax.swing.JPanel();
		lblProxyHost = new javax.swing.JLabel();
		txtProxyHost = new javax.swing.JTextField();
		lblProxyPort = new javax.swing.JLabel();
		txtProxyPort = new javax.swing.JTextField();
		lblProxyUsername = new javax.swing.JLabel();
		txtProxyUsername = new javax.swing.JTextField();
		lblProxyPassword = new javax.swing.JLabel();
		txtProxyPassword = new javax.swing.JPasswordField();
		pnlButton = new javax.swing.JPanel();
		btnOK = new javax.swing.JButton();
		btnCancel = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle(i18n.get("config.title")); // NOI18N
		setIconImages(null);
		setResizable(false);
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				ConfigFrame.this.windowClosing(evt);
			}
		});

		pnlLocal.setBorder(javax.swing.BorderFactory.createTitledBorder(i18n.get("config.local"))); // NOI18N

		lblServers.setText(i18n.get("config.servers")); // NOI18N

		lstServers.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				lstServersMouseClicked(evt);
			}
		});
		sclServers.setViewportView(lstServers);

		btnAddServer.setText(i18n.get("config.action.add-server")); // NOI18N
		btnAddServer.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnAddServerActionPerformed(evt);
			}
		});

		btnEditServer.setText(i18n.get("config.action.edit-server")); // NOI18N
		btnEditServer.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnEditServerActionPerformed(evt);
			}
		});

		btnRemoveServer.setText(i18n.get("config.action.remove-server")); // NOI18N
		btnRemoveServer.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnRemoveServerActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout pnlLocalLayout = new javax.swing.GroupLayout(pnlLocal);
		pnlLocal.setLayout(pnlLocalLayout);
		pnlLocalLayout.setHorizontalGroup(
				pnlLocalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(pnlLocalLayout.createSequentialGroup()
								.addContainerGap()
								.addComponent(lblServers)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(sclServers, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(pnlLocalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(btnAddServer, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(btnRemoveServer, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(btnEditServer, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		pnlLocalLayout.setVerticalGroup(
				pnlLocalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(pnlLocalLayout.createSequentialGroup()
								.addGroup(pnlLocalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(sclServers, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
										.addGroup(pnlLocalLayout.createSequentialGroup()
												.addGroup(pnlLocalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(lblServers)
														.addComponent(btnAddServer))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(btnEditServer)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(btnRemoveServer)
												.addGap(0, 0, Short.MAX_VALUE)))
								.addContainerGap())
				);

		pnlProxy.setBorder(javax.swing.BorderFactory.createTitledBorder(i18n.get("config.proxy"))); // NOI18N

		lblProxyHost.setText(i18n.get("config.proxy-host")); // NOI18N

		lblProxyPort.setText(i18n.get("config.proxy-port")); // NOI18N

		lblProxyUsername.setText(i18n.get("config.proxy-username")); // NOI18N

		lblProxyPassword.setText(i18n.get("config.proxy-password")); // NOI18N

		javax.swing.GroupLayout pnlProxyLayout = new javax.swing.GroupLayout(pnlProxy);
		pnlProxy.setLayout(pnlProxyLayout);
		pnlProxyLayout.setHorizontalGroup(
				pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(pnlProxyLayout.createSequentialGroup()
								.addContainerGap()
								.addGroup(pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(lblProxyHost)
										.addComponent(lblProxyUsername)
										.addComponent(lblProxyPort)
										.addComponent(lblProxyPassword))
								.addGap(18, 18, 18)
								.addGroup(pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(txtProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(txtProxyHost)
										.addComponent(txtProxyUsername, javax.swing.GroupLayout.Alignment.TRAILING)
										.addComponent(txtProxyPassword, javax.swing.GroupLayout.Alignment.TRAILING))
								.addContainerGap())
				);
		pnlProxyLayout.setVerticalGroup(
				pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(pnlProxyLayout.createSequentialGroup()
								.addGroup(pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblProxyHost)
												.addComponent(txtProxyHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGroup(pnlProxyLayout.createSequentialGroup()
												.addGap(26, 26, 26)
												.addGroup(pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(lblProxyPort)
														.addComponent(txtProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblProxyUsername)
										.addComponent(txtProxyUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(txtProxyPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(lblProxyPassword))
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		btnOK.setText(i18n.get("dialog.ok")); // NOI18N
		btnOK.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnOKActionPerformed(evt);
			}
		});

		btnCancel.setText(i18n.get("dialog.cancel")); // NOI18N
		btnCancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnCancelActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout pnlButtonLayout = new javax.swing.GroupLayout(pnlButton);
		pnlButton.setLayout(pnlButtonLayout);
		pnlButtonLayout.setHorizontalGroup(
				pnlButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlButtonLayout.createSequentialGroup()
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(btnOK, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addContainerGap())
				);
		pnlButtonLayout.setVerticalGroup(
				pnlButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(pnlButtonLayout.createSequentialGroup()
								.addGroup(pnlButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(btnCancel)
										.addComponent(btnOK))
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addComponent(pnlButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(pnlLocal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(pnlProxy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addContainerGap())
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addContainerGap()
								.addComponent(pnlLocal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(pnlProxy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(pnlButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				);

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
		if (saveConfig()) {
			close();
		}
	}//GEN-LAST:event_btnOKActionPerformed

	private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
		close();
	}//GEN-LAST:event_btnCancelActionPerformed

	private void windowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_windowClosing
		close();
	}//GEN-LAST:event_windowClosing

	private void btnAddServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddServerActionPerformed
		ServerFrame.showDialog(this, new ServerConfig());
	}//GEN-LAST:event_btnAddServerActionPerformed
	private void btnEditServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditServerActionPerformed
		ServerConfig server = (ServerConfig) lstServers.getSelectedValue();
		if (server != null) {
			ServerFrame.showDialog(this, server);
		}
	}//GEN-LAST:event_btnEditServerActionPerformed

	private void btnRemoveServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveServerActionPerformed
		ServerConfig server = (ServerConfig) lstServers.getSelectedValue();
		if (server != null) {
			lstServersModel.removeElement(server);
		}
	}//GEN-LAST:event_btnRemoveServerActionPerformed

	private void lstServersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstServersMouseClicked
		if (evt.getClickCount() == 2) {
			btnEditServerActionPerformed(null);
		}
	}//GEN-LAST:event_lstServersMouseClicked

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton btnAddServer;
	private javax.swing.JButton btnCancel;
	private javax.swing.JButton btnEditServer;
	private javax.swing.JButton btnOK;
	private javax.swing.JButton btnRemoveServer;
	private javax.swing.JLabel lblProxyHost;
	private javax.swing.JLabel lblProxyPassword;
	private javax.swing.JLabel lblProxyPort;
	private javax.swing.JLabel lblProxyUsername;
	private javax.swing.JLabel lblServers;
	private javax.swing.JList lstServers;
	private javax.swing.JPanel pnlButton;
	private javax.swing.JPanel pnlLocal;
	private javax.swing.JPanel pnlProxy;
	private javax.swing.JScrollPane sclServers;
	private javax.swing.JTextField txtProxyHost;
	private javax.swing.JPasswordField txtProxyPassword;
	private javax.swing.JTextField txtProxyPort;
	private javax.swing.JTextField txtProxyUsername;
	// End of variables declaration//GEN-END:variables

}
