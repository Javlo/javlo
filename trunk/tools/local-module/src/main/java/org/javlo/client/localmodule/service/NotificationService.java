package org.javlo.client.localmodule.service;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Stack;

import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.Timer;

import org.javlo.service.IMService.IMItem;

public class NotificationService {

	private Stack<JWindow> notificationWindows = new Stack<JWindow>();

	public void displayIMMessage(IMItem item) {
		Rectangle desktop = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

		synchronized (notificationWindows) {
			int baseLine;
			int baseColumn = desktop.x + desktop.width;

			if (!notificationWindows.empty()) {
				JWindow lastNotif = notificationWindows.peek();
				baseLine = lastNotif.getY();
			} else {
				baseLine = desktop.y + desktop.height;
			}
			JLabel lblMessage = new JLabel();
			lblMessage.setText(item.getMessage());
			lblMessage.setPreferredSize(new Dimension(150, 100));
			final JWindow notifWindow = new JWindow();
			notifWindow.getContentPane().add(lblMessage);
			notifWindow.pack();
			notifWindow.setLocation(baseColumn - notifWindow.getWidth(), baseLine - notifWindow.getHeight());
			notifWindow.setAlwaysOnTop(true);
			notifWindow.setVisible(true);
			notificationWindows.push(notifWindow);
			notifWindow.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					notificationWindows.removeElement(notifWindow);
					notifWindow.dispose();
				}
			});
			new Timer(5000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					notifWindow.setVisible(false);
					notificationWindows.removeElement(notifWindow);
					notifWindow.dispose();
					((Timer) e.getSource()).stop();
				}
			}).start();
		}
	}

}
