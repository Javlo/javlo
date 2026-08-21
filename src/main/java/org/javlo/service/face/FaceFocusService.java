package org.javlo.service.face;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.javlo.face.Face;
import org.javlo.face.FaceDetector;
import org.javlo.helper.StringHelper;
import org.javlo.image.ImageEngine;

/**
 * Places the focus point of a picture on the faces it contains, so a cropped thumbnail keeps the
 * people in the frame instead of cutting their heads.
 *
 * The detection runs locally on the CPU with the YuNet model of the <code>javlo-face</code> module,
 * which lives in its own repository and is installed by ./get_javlo_face.sh : no network call and
 * 35 ms per picture once the model is loaded. The model is loaded once and the detector is shared,
 * as it is thread safe.
 *
 * The focus point is returned the way Javlo stores it, in per mille of the picture, ready for
 * {@link org.javlo.ztatic.StaticInfo#setFocusZoneX(org.javlo.context.ContentContext, int)}.
 */
public class FaceFocusService {

	private static Logger logger = Logger.getLogger(FaceFocusService.class.getName());

	/**
	 * A face smaller than this share of the biggest one is a passer-by, not a subject : it must not
	 * drag the focus towards the background.
	 */
	static final double MIN_RELATIVE_FACE_AREA = 0.2;

	private static FaceFocusService instance = null;

	private FaceDetector detector = null;

	/** Set once the detector could not be created : we then stop trying on every picture. */
	private boolean unavailable = false;

	private FaceFocusService() {
	}

	public static synchronized FaceFocusService getInstance() {
		if (instance == null) {
			instance = new FaceFocusService();
		}
		return instance;
	}

	/**
	 * The detector is optional : the onnxruntime jar weights 90 Mo and can be left out of the war. When
	 * it is missing, face detection is simply switched off and Javlo keeps working.
	 */
	public synchronized boolean isActive() {
		if (unavailable) {
			return false;
		}
		if (detector == null) {
			try {
				long time = System.currentTimeMillis();
				detector = new FaceDetector();
				logger.info("face detection model loaded in " + (System.currentTimeMillis() - time) + " ms");
			} catch (Throwable t) {
				// NoClassDefFoundError when the jar is not deployed, IOException when the model fails
				unavailable = true;
				logger.warning("face detection not available : " + t.getMessage());
				return false;
			}
		}
		return true;
	}

	/**
	 * Finds the point of a picture the crop should be centred on.
	 *
	 * @param file the picture on disk
	 * @return the focus point in per mille of the picture (0 to 1000 on both axis), or null when there
	 *         is no face, no picture, or no detector
	 */
	public Point getFocusPoint(File file) {
		if (file == null || !file.exists() || !StringHelper.isImage(file.getName())) {
			return null;
		}
		if (!isActive()) {
			return null;
		}
		try {
			// the same loading as the transform servlet, so the EXIF rotation is applied here too and
			// the coordinates match the picture the visitor sees
			BufferedImage image = ImageEngine.loadImage(file);
			if (image == null) {
				logger.warning("can not read picture : " + file);
				return null;
			}
			long time = System.currentTimeMillis();
			List<Face> faces = detector.detect(image);
			List<Rectangle> boxes = new ArrayList<Rectangle>(faces.size());
			for (Face face : faces) {
				boxes.add(face.getBounds());
			}
			Point focus = focusPoint(boxes, image.getWidth(), image.getHeight());
			logger.info("face detection on " + file.getName() + " : " + faces.size() + " face(s) in "
					+ (System.currentTimeMillis() - time) + " ms, focus " + focus);
			return focus;
		} catch (Throwable t) {
			logger.warning("error on face detection on " + file + " : " + t.getMessage());
			return null;
		}
	}

	/**
	 * Turns the boxes of the detected faces into the focus point stored by Javlo.
	 *
	 * The point is the centre of the box enclosing the faces that matter : that way a group photo keeps
	 * everybody in the frame, while a portrait is centred on the only face. Faces much smaller than the
	 * biggest one are left out, they belong to the background.
	 *
	 * @return the focus point in per mille, or null when there is nothing to focus on
	 */
	static Point focusPoint(List<Rectangle> faces, int imageWidth, int imageHeight) {
		if (faces == null || faces.isEmpty() || imageWidth <= 0 || imageHeight <= 0) {
			return null;
		}
		long largestArea = 0;
		for (Rectangle face : faces) {
			largestArea = Math.max(largestArea, (long) face.width * face.height);
		}
		Rectangle subjects = null;
		for (Rectangle face : faces) {
			if ((long) face.width * face.height >= largestArea * MIN_RELATIVE_FACE_AREA) {
				subjects = subjects == null ? new Rectangle(face) : subjects.union(face);
			}
		}
		if (subjects == null) {
			return null;
		}
		int x = (int) Math.round(subjects.getCenterX() * 1000 / imageWidth);
		int y = (int) Math.round(subjects.getCenterY() * 1000 / imageHeight);
		return new Point(bound(x), bound(y));
	}

	private static int bound(int perMille) {
		return Math.min(1000, Math.max(0, perMille));
	}
}
