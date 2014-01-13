package org.javlo.client.localmodule;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;

import sun.awt.AWTAccessor;

public class SlideInNotification extends Object {

	protected static final int ANIMATION_TIME = 500;
	protected static final float ANIMATION_TIME_F =
			(float) ANIMATION_TIME;
	protected static final int ANIMATION_DELAY = 50;

	JWindow window;
	JComponent contents;
	AnimatingSheet animatingSheet;
	Rectangle desktopBounds;
	Dimension tempWindowSize;
	Timer animationTimer;
	int showX, startY;
	long animationStart;

	public SlideInNotification() {
		initDesktopBounds();
	}

	public SlideInNotification(JComponent contents) {
		this();
		setContents(contents);
	}

	protected void initDesktopBounds() {
		GraphicsEnvironment env =
				GraphicsEnvironment.getLocalGraphicsEnvironment();
		desktopBounds = env.getMaximumWindowBounds();
		System.out.println("max window bounds = " + desktopBounds);
	}

	public void setContents(JComponent contents) {
		this.contents = contents;
		JWindow tempWindow = new JWindow();
		tempWindow.getContentPane().add(contents);
		tempWindow.pack();
		tempWindowSize = tempWindow.getSize();
		tempWindow.getContentPane().removeAll();
		window = new JWindow();
		//window.setResizable(false);
		//window.setIconImage(null);
		window.setAlwaysOnTop(true);
		//window.setTitle("Message");
		AWTAccessor.getWindowAccessor().setOpacity(window, 0.8F);
		animatingSheet = new AnimatingSheet();
		animatingSheet.setSource(contents);
		window.getContentPane().add(animatingSheet);
	}

	public void showAt(int x) {
		// create a window with an animating sheet
		// copy over its contents from the temp window
		// animate it
		// when done, remove animating sheet and add real contents

		showX = desktopBounds.x + desktopBounds.width;
		startY = desktopBounds.y + desktopBounds.height;

		ActionListener animationLogic = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				long elapsed =
						System.currentTimeMillis() - animationStart;
				if (elapsed > ANIMATION_TIME) {
					// put real contents in window and show
					window.getContentPane().removeAll();
					window.getContentPane().add(contents);
					window.pack();
					window.setLocation(showX - window.getSize().width,
							startY - window.getSize().height);
					window.setVisible(true);
					window.repaint();
					animationTimer.stop();
					animationTimer = null;
				} else {
					// calculate % done
					float progress =
							(float) elapsed / ANIMATION_TIME_F;
					// get height to show
					int animatingHeight =
							(int) (progress * tempWindowSize.getHeight());
					//System.out.println ("animatingHeight " + 
					//                    animatingHeight);
					animatingHeight = Math.max(animatingHeight, 1);
					animatingSheet.setAnimatingHeight(animatingHeight);
					window.pack();
					window.setLocation(showX - window.getSize().width,
							startY - window.getHeight());
					window.setVisible(true);
					window.repaint();
				}
			}
		};
		animationTimer =
				new Timer(ANIMATION_DELAY, animationLogic);
		animationStart = System.currentTimeMillis();
		animationTimer.start();
	}

	class AnimatingSheet extends JPanel {
		Dimension animatingSize = new Dimension(0, 1);
		JComponent source;
		BufferedImage offscreenImage;
		public AnimatingSheet() {
			super();
			setOpaque(true);
		}
		public void setSource(JComponent source) {
			this.source = source;
			animatingSize.width = source.getWidth();
			makeOffscreenImage(source);
		}
		public void setAnimatingHeight(int height) {
			animatingSize.height = height;
			setSize(animatingSize);
		}
		private void makeOffscreenImage(JComponent source) {
			GraphicsEnvironment ge =
					GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsConfiguration gfxConfig =
					ge.getDefaultScreenDevice().getDefaultConfiguration();
			offscreenImage =
					gfxConfig.createCompatibleImage(source.getWidth(),
							source.getHeight());
			Graphics2D offscreenGraphics =
					(Graphics2D) offscreenImage.getGraphics();
			// windows workaround
			offscreenGraphics.setColor(source.getBackground());
			offscreenGraphics.fillRect(0, 0,
					source.getWidth(), source.getHeight());
			// paint from source to offscreen buffer
			source.paint(offscreenGraphics);
		}
		public Dimension getPreferredSize() {
			return animatingSize;
		}
		public Dimension getMinimumSize() {
			return animatingSize;
		}
		public Dimension getMaximumSize() {
			return animatingSize;
		}
		public void update(Graphics g) {
			// override to eliminate flicker from
			// unneccessary clear
			paint(g);
		}
		public void paint(Graphics g) {
			// get the top-most n pixels of source and 
			// paint them into g, where n is height
			// (different from sheet example, which used bottom-most)
			BufferedImage fragment =
					offscreenImage.getSubimage(0,
							0,
							source.getWidth(),
							animatingSize.height);
			g.drawImage(fragment, 0, 0, this);
		}
	}
}
