/*
 * ConfigFrame.java
 *
 * Created on 27-oct.-2010, 16:31:00
 */

package org.javlo.client.localmodule.ui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.javlo.client.localmodule.service.ConfigService;
import org.javlo.client.localmodule.service.I18nService;
import org.javlo.client.localmodule.service.SynchroControlService;
import org.javlo.helper.StringHelper;

/**
 *
 * @author bdumont
 */
public class ConfigFrame extends javax.swing.JDialog {

	private static final long serialVersionUID = 7060664378303938141L;

	private static ConfigFrame instance;

	private I18nService i18n = I18nService.getInstance();
	private ConfigService config = ConfigService.getInstance();

	private boolean passwordCleared = false;

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
		txtComputerName.setText(config.getComputerName());
		String localFolder = config.getLocalFolder();
		if (localFolder == null) {
			localFolder = StringHelper.expandSystemProperties(ConfigService.DEFAULT_FOLDER);
		}
		txtLocalFolder.setText(localFolder);
		txtServerURL.setText(config.getServerURL());
		txtUsername.setText(config.getUsername());
		txtPassword.setText(config.getPassword());
		chkStorePassword.setSelected(config.isStorePassword());
		txtProxyHost.setText(config.getProxyHost());
		if (config.getProxyPort() == null) {
			txtProxyPort.setText(null);
		} else {
			txtProxyPort.setText("" + config.getProxyPort());
		}
		txtProxyUsername.setText(config.getProxyUsername());
		txtProxyPassword.setText(config.getProxyPassword());
	}

	private boolean saveConfig() {
		//Parse an check input values
		String computerName = StringHelper.trimAndNullify(txtComputerName.getText());
		if (computerName == null) {
			JOptionPane.showMessageDialog(this, i18n.get("error.empty-computer-name"), i18n.get("error"), JOptionPane.ERROR_MESSAGE);
			txtComputerName.requestFocus();
			return false;
		}
		String localFolder = StringHelper.trimAndNullify(txtLocalFolder.getText());
		if (!config.checkLocalFolder(localFolder)) {
			JOptionPane.showMessageDialog(this, i18n.get("error.invalid-local-folder"), i18n.get("error"), JOptionPane.ERROR_MESSAGE);
			txtLocalFolder.requestFocus();
			return false;
		}
		File newLF = new File(localFolder);
		File presentLF = null;
		if (config.getLocalFolder() != null) {
			presentLF = new File(config.getLocalFolder());
		}
		if (!newLF.equals(presentLF)) {
			boolean newIsEmpty = (newLF.list().length == 0);
			if (!newIsEmpty) {
				JOptionPane.showMessageDialog(this, i18n.get("error.local-folder-not-empty"), i18n.get("error"), JOptionPane.ERROR_MESSAGE);
				return false;
			}
			//if (presentLF != null && presentLF.exists()) {
			//	//TODO do you want to move
			//}
		}

		String serverURL = StringHelper.trimAndNullify(txtServerURL.getText());
		if (!config.checkServerURL(serverURL)) {
			JOptionPane.showMessageDialog(this, i18n.get("error.invalid-server-url"), i18n.get("error"), JOptionPane.ERROR_MESSAGE);
			txtServerURL.requestFocus();
			return false;
		}
		String username = StringHelper.trimAndNullify(txtUsername.getText());
		if (username == null) {
			JOptionPane.showMessageDialog(this, i18n.get("error.empty-username"), i18n.get("error"), JOptionPane.ERROR_MESSAGE);
			txtUsername.requestFocus();
			return false;
		}
		String password = StringHelper.trimAndNullify(new String(txtPassword.getPassword()));
		if (password == null) {
			JOptionPane.showMessageDialog(this, i18n.get("error.empty-password"), i18n.get("error"), JOptionPane.ERROR_MESSAGE);
			txtPassword.requestFocus();
			return false;
		}
		String proxyHost = StringHelper.trimAndNullify(txtProxyHost.getText());
		String proxyPortStr = StringHelper.trimAndNullify(txtProxyPort.getText());
		Integer proxyPort = null;
		if (proxyPortStr != null) {
			try {
				proxyPort = Integer.parseInt(txtProxyPort.getText());
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, i18n.get("error.invalid-proxy-port") + '\n' + i18n.get("error.details") + ex.getLocalizedMessage(), i18n.get("error"), JOptionPane.ERROR_MESSAGE);
				txtPassword.requestFocus();
				return false;
			}
			if (proxyPort < 1 || proxyPort > 65535) {
				JOptionPane.showMessageDialog(this, i18n.get("error.invalid-proxy-port"), i18n.get("error"), JOptionPane.ERROR_MESSAGE);
				txtPassword.requestFocus();
				return false;
			}
		}
		if (proxyHost != null && proxyPort == null) {
			JOptionPane.showMessageDialog(this, i18n.get("error.empty-proxy-port"), i18n.get("error"), JOptionPane.ERROR_MESSAGE);
			txtPassword.requestFocus();
			return false;
		}

		//Store
		synchronized (config.lock) {
			config.setComputerName(computerName);
			config.setLocalFolder(localFolder);
			config.setServerURL(serverURL);
			config.setUsername(username);
			config.setPassword(password);
			config.setStorePassword(chkStorePassword.isSelected());
			config.setProxyHost(proxyHost);
			config.setProxyPort(proxyPort);
			config.setProxyUsername(StringHelper.trimAndNullify(txtProxyUsername.getText()));
			config.setProxyPassword(StringHelper.trimAndNullify(new String(txtProxyPassword.getPassword())));
			try {
				config.save();
				SynchroControlService.getInstance().start();
				return true;
			} catch (Exception ex) {
				try {
					config.reload();
				} catch (Exception ex2) {
				}
				JOptionPane.showMessageDialog(this, i18n.get("error.config-op"), i18n.get("error"), JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
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
		lblComputerName = new javax.swing.JLabel();
		txtComputerName = new javax.swing.JTextField();
		lblLocalFolder = new javax.swing.JLabel();
		txtLocalFolder = new javax.swing.JTextField();
		btnLocalBrowse = new javax.swing.JButton();
		pnlServer = new javax.swing.JPanel();
		lblServerURL = new javax.swing.JLabel();
		txtServerURL = new javax.swing.JTextField();
		lblUsername = new javax.swing.JLabel();
		txtUsername = new javax.swing.JTextField();
		lblPassword = new javax.swing.JLabel();
		txtPassword = new javax.swing.JPasswordField();
		chkStorePassword = new javax.swing.JCheckBox();
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

		lblComputerName.setText(i18n.get("config.computer-name")); // NOI18N

		lblLocalFolder.setText(i18n.get("config.local-folder")); // NOI18N

		txtLocalFolder.setEditable(false);

		btnLocalBrowse.setText("...");
		btnLocalBrowse.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnLocalBrowseActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout pnlLocalLayout = new javax.swing.GroupLayout(pnlLocal);
		pnlLocal.setLayout(pnlLocalLayout);
		pnlLocalLayout.setHorizontalGroup(pnlLocalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				pnlLocalLayout
						.createSequentialGroup()
						.addContainerGap()
						.addGroup(pnlLocalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(lblComputerName).addComponent(lblLocalFolder))
						.addGap(18, 18, 18)
						.addGroup(
								pnlLocalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
										.addGroup(pnlLocalLayout.createSequentialGroup().addComponent(txtLocalFolder, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(btnLocalBrowse))
										.addComponent(txtComputerName, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)).addContainerGap()));
		pnlLocalLayout.setVerticalGroup(pnlLocalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				pnlLocalLayout
						.createSequentialGroup()
						.addGroup(pnlLocalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(lblComputerName).addComponent(txtComputerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(
								pnlLocalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(lblLocalFolder).addComponent(txtLocalFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(btnLocalBrowse)).addContainerGap(6, Short.MAX_VALUE)));

		pnlServer.setBorder(javax.swing.BorderFactory.createTitledBorder(i18n.get("config.server"))); // NOI18N

		lblServerURL.setText(i18n.get("config.server-url")); // NOI18N

		lblUsername.setText(i18n.get("config.username")); // NOI18N

		lblPassword.setText(i18n.get("config.password")); // NOI18N

		chkStorePassword.setText(i18n.get("config.store-password")); // NOI18N
		chkStorePassword.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				chkStorePasswordActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout pnlServerLayout = new javax.swing.GroupLayout(pnlServer);
		pnlServer.setLayout(pnlServerLayout);
		pnlServerLayout.setHorizontalGroup(pnlServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				pnlServerLayout
						.createSequentialGroup()
						.addContainerGap()
						.addGroup(pnlServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(lblServerURL).addComponent(lblUsername).addComponent(lblPassword))
						.addGap(18, 18, 18)
						.addGroup(
								pnlServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(chkStorePassword).addComponent(txtPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
										.addComponent(txtServerURL, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
										.addComponent(txtUsername, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)).addContainerGap()));
		pnlServerLayout.setVerticalGroup(pnlServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				pnlServerLayout.createSequentialGroup()
						.addGroup(pnlServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(lblServerURL).addComponent(txtServerURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(pnlServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(lblUsername))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(pnlServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(lblPassword))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(chkStorePassword)));

		pnlProxy.setBorder(javax.swing.BorderFactory.createTitledBorder(i18n.get("config.proxy"))); // NOI18N

		lblProxyHost.setText(i18n.get("config.proxy-host")); // NOI18N

		lblProxyPort.setText(i18n.get("config.proxy-port")); // NOI18N

		lblProxyUsername.setText(i18n.get("config.proxy-username")); // NOI18N

		lblProxyPassword.setText(i18n.get("config.proxy-password")); // NOI18N

		javax.swing.GroupLayout pnlProxyLayout = new javax.swing.GroupLayout(pnlProxy);
		pnlProxy.setLayout(pnlProxyLayout);
		pnlProxyLayout.setHorizontalGroup(pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				pnlProxyLayout
						.createSequentialGroup()
						.addContainerGap()
						.addGroup(pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(lblProxyHost).addComponent(lblProxyUsername).addComponent(lblProxyPort).addComponent(lblProxyPassword))
						.addGap(18, 18, 18)
						.addGroup(
								pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(txtProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(txtProxyHost, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE).addComponent(txtProxyUsername, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
										.addComponent(txtProxyPassword, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)).addContainerGap()));
		pnlProxyLayout.setVerticalGroup(pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				pnlProxyLayout
						.createSequentialGroup()
						.addGroup(
								pnlProxyLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(
												pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(lblProxyHost).addComponent(txtProxyHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGroup(
												pnlProxyLayout
														.createSequentialGroup()
														.addGap(26, 26, 26)
														.addGroup(
																pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(lblProxyPort)
																		.addComponent(txtProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(lblProxyUsername).addComponent(txtProxyUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(txtProxyPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(lblProxyPassword))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

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
		pnlButtonLayout.setHorizontalGroup(pnlButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				javax.swing.GroupLayout.Alignment.TRAILING,
				pnlButtonLayout.createSequentialGroup().addContainerGap(212, Short.MAX_VALUE).addComponent(btnOK, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap()));
		pnlButtonLayout.setVerticalGroup(pnlButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				pnlButtonLayout.createSequentialGroup().addGroup(pnlButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(btnCancel).addComponent(btnOK)).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(pnlButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(
						javax.swing.GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(pnlServer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(pnlLocal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap())
				.addGroup(layout.createSequentialGroup().addContainerGap().addComponent(pnlProxy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup().addContainerGap().addComponent(pnlLocal, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(pnlServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(pnlProxy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(pnlButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void btnLocalBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLocalBrowseActionPerformed
		File currentDirectory = new File(txtLocalFolder.getText());
		JFileChooser chooser = new JFileChooser(currentDirectory);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		chooser.setSelectedFile(currentDirectory);
		if (chooser.showDialog(this, i18n.get("config.browse-select")) == JFileChooser.APPROVE_OPTION) {
			txtLocalFolder.setText(chooser.getSelectedFile().getAbsolutePath());
		}

	}//GEN-LAST:event_btnLocalBrowseActionPerformed

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

	private void chkStorePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkStorePasswordActionPerformed
		if (!passwordCleared && chkStorePassword.isSelected() && !config.isStorePassword() && new String(txtPassword.getPassword()).equals(config.getPassword())) {
			txtPassword.setText("");
			passwordCleared = true;
		}
	}//GEN-LAST:event_chkStorePasswordActionPerformed

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton btnCancel;
	private javax.swing.JButton btnLocalBrowse;
	private javax.swing.JButton btnOK;
	private javax.swing.JCheckBox chkStorePassword;
	private javax.swing.JLabel lblComputerName;
	private javax.swing.JLabel lblLocalFolder;
	private javax.swing.JLabel lblPassword;
	private javax.swing.JLabel lblProxyHost;
	private javax.swing.JLabel lblProxyPassword;
	private javax.swing.JLabel lblProxyPort;
	private javax.swing.JLabel lblProxyUsername;
	private javax.swing.JLabel lblServerURL;
	private javax.swing.JLabel lblUsername;
	private javax.swing.JPanel pnlButton;
	private javax.swing.JPanel pnlLocal;
	private javax.swing.JPanel pnlProxy;
	private javax.swing.JPanel pnlServer;
	private javax.swing.JTextField txtComputerName;
	private javax.swing.JTextField txtLocalFolder;
	private javax.swing.JPasswordField txtPassword;
	private javax.swing.JTextField txtProxyHost;
	private javax.swing.JPasswordField txtProxyPassword;
	private javax.swing.JTextField txtProxyPort;
	private javax.swing.JTextField txtProxyUsername;
	private javax.swing.JTextField txtServerURL;
	private javax.swing.JTextField txtUsername;
	// End of variables declaration//GEN-END:variables

}
