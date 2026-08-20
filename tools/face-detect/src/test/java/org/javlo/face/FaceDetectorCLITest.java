package org.javlo.face;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FaceDetectorCLITest {

	private Locale original;

	@Before
	public void useAFrenchLocale() {
		original = Locale.getDefault();
		Locale.setDefault(Locale.FRANCE);
	}

	@After
	public void restoreLocale() {
		Locale.setDefault(original);
	}

	private static String json(List<Face> faces) {
		return FaceDetectorCLI.toJson(new File("photo.jpg"),
				new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB), faces, 42);
	}

	/** A locale using the comma as a decimal separator must not produce broken JSON. */
	@Test
	public void writesScoresWithADotWhateverTheLocale() {
		Point[] landmarks = { new Point(1, 2), new Point(3, 4), new Point(5, 6), new Point(7, 8),
				new Point(9, 10) };
		String json = json(Collections.singletonList(new Face(new Rectangle(10, 20, 30, 40), 0.9434f, landmarks)));

		assertTrue(json, json.contains("\"score\": 0.9434"));
		assertFalse("a comma would break the JSON: " + json, json.contains("0,9434"));
	}

	@Test
	public void escapesBackslashesOfWindowsPaths() {
		String json = FaceDetectorCLI.toJson(new File("C:\\photos\\a.jpg"),
				new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), Collections.emptyList(), 1);

		assertTrue(json, json.contains("C:\\\\photos\\\\a.jpg"));
	}

	@Test
	public void staysValidWhenNoFaceIsFound() {
		String json = json(Collections.emptyList());

		assertTrue(json, json.contains("\"faceCount\": 0"));
		assertTrue(json, json.contains("\"faces\": []"));
	}
}
