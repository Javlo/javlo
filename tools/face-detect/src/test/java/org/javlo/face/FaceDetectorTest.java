package org.javlo.face;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * End to end test on a real photograph: astronaut.png is a public domain NASA portrait of Eileen
 * Collins, 512x512 pixels, holding exactly one face in the upper half of the frame.
 */
public class FaceDetectorTest {

	private static FaceDetector detector;
	private static BufferedImage photo;

	@BeforeClass
	public static void setUp() throws Exception {
		detector = new FaceDetector();
		try (InputStream in = FaceDetectorTest.class.getResourceAsStream("/astronaut.png")) {
			photo = ImageIO.read(in);
		}
	}

	@AfterClass
	public static void tearDown() {
		if (detector != null) {
			detector.close();
		}
	}

	@Test
	public void findsTheSingleFaceOfThePhoto() {
		List<Face> faces = detector.detect(photo);

		assertEquals(1, faces.size());
		Face face = faces.get(0);
		assertTrue("weak score: " + face.getScore(), face.getScore() > 0.8f);

		Rectangle bounds = face.getBounds();
		assertTrue("face outside the image: " + bounds,
				new Rectangle(0, 0, photo.getWidth(), photo.getHeight()).contains(bounds));
		assertTrue("implausible width: " + bounds.width,
				bounds.width > photo.getWidth() * 0.05 && bounds.width < photo.getWidth() * 0.6);
		assertTrue("the face should sit in the upper half: " + bounds,
				bounds.getCenterY() < photo.getHeight() / 2.0);
	}

	@Test
	public void landmarksSitInsideTheFace() {
		Face face = detector.detect(photo).get(0);
		Rectangle bounds = face.getBounds();

		assertTrue(bounds.contains(face.getRightEye()));
		assertTrue(bounds.contains(face.getLeftEye()));
		assertTrue(bounds.contains(face.getNose()));
		// the right eye of the subject is on the left hand side of the picture
		assertTrue(face.getRightEye().x < face.getLeftEye().x);
		// eyes sit above the mouth
		assertTrue(face.getRightEye().y < face.getRightMouthCorner().y);
	}

	@Test
	public void mainFaceReturnsTheBoxOfTheDetectedFace() {
		Rectangle main = detector.detectMainFace(photo);

		assertNotNull(main);
		assertEquals(detector.detect(photo).get(0).getBounds(), main);
	}

	@Test
	public void returnsNothingOnAnImageWithoutFace() {
		BufferedImage blank = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);

		assertTrue(detector.detect(blank).isEmpty());
		assertNull(detector.detectMainFace(blank));
		assertNull(detector.detectFacesBounds(blank));
	}

	@Test
	public void findsEveryFaceOfAMontage() {
		// the photo tiled twice by twice holds four faces, far enough apart not to be merged
		BufferedImage montage = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
		for (int col = 0; col < 2; col++) {
			for (int row = 0; row < 2; row++) {
				montage.getGraphics().drawImage(photo, col * 512, row * 512, null);
			}
		}

		List<Face> faces = detector.detect(montage);

		assertEquals(4, faces.size());
		// one face per quadrant
		for (int col = 0; col < 2; col++) {
			for (int row = 0; row < 2; row++) {
				Rectangle quadrant = new Rectangle(col * 512, row * 512, 512, 512);
				long inQuadrant = faces.stream().filter(f -> quadrant.contains(f.getCenter())).count();
				assertEquals("quadrant " + col + "," + row, 1, inQuadrant);
			}
		}
	}

	@Test
	public void keepsTheAspectRatioOfNonSquareImages() {
		// the same photo padded into a wide canvas must yield the same face, shifted by the padding
		BufferedImage wide = new BufferedImage(1024, 512, BufferedImage.TYPE_INT_RGB);
		wide.getGraphics().drawImage(photo, 256, 0, null);

		List<Face> faces = detector.detect(wide);

		assertEquals(1, faces.size());
		Rectangle expected = detector.detect(photo).get(0).getBounds();
		Rectangle actual = faces.get(0).getBounds();
		assertEquals("x shifted by the padding", expected.x + 256, actual.x, 12);
		assertEquals(expected.y, actual.y, 12);
		assertEquals(expected.width, actual.width, 12);
	}
}
