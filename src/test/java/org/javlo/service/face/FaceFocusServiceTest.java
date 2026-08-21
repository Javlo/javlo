package org.javlo.service.face;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

/**
 * Tests the geometry only : turning the boxes given by the detector into the focus point Javlo stores,
 * expressed in per mille of the picture. No model is loaded here.
 */
public class FaceFocusServiceTest extends TestCase {

	public void testNoFaceGivesNoFocus() {
		assertNull(FaceFocusService.focusPoint(Collections.<Rectangle> emptyList(), 800, 600));
	}

	public void testEmptyImageGivesNoFocus() {
		assertNull(FaceFocusService.focusPoint(Arrays.asList(new Rectangle(10, 10, 10, 10)), 0, 600));
	}

	public void testSingleFaceFocusOnItsCentre() {
		// centre of the box : 200,150 - the picture is 1000 wide and 500 high
		Point focus = FaceFocusService.focusPoint(Arrays.asList(new Rectangle(150, 100, 100, 100)), 1000, 500);
		assertEquals(200, focus.x);
		assertEquals(300, focus.y);
	}

	public void testCentredFaceGivesTheDefaultFocus() {
		Point focus = FaceFocusService.focusPoint(Arrays.asList(new Rectangle(400, 400, 200, 200)), 1000, 1000);
		assertEquals(500, focus.x);
		assertEquals(500, focus.y);
	}

	public void testTwoFacesKeepBothInTheFrame() {
		// two faces of the same size, one on each side : the focus sits between them
		List<Rectangle> faces = Arrays.asList(new Rectangle(100, 400, 100, 100), new Rectangle(700, 400, 100, 100));
		Point focus = FaceFocusService.focusPoint(faces, 1000, 1000);
		assertEquals(450, focus.x);
		assertEquals(450, focus.y);
	}

	public void testSmallBackgroundFaceIsIgnored() {
		// the second face covers 1% of the first one : a passer-by, not the subject
		List<Rectangle> faces = Arrays.asList(new Rectangle(100, 100, 200, 200), new Rectangle(900, 900, 20, 20));
		Point focus = FaceFocusService.focusPoint(faces, 1000, 1000);
		assertEquals(200, focus.x);
		assertEquals(200, focus.y);
	}

	public void testFaceOfComparableSizeIsKept() {
		// half the area of the biggest one : still one of the subjects
		List<Rectangle> faces = Arrays.asList(new Rectangle(100, 100, 200, 200), new Rectangle(600, 100, 140, 140));
		Point focus = FaceFocusService.focusPoint(faces, 1000, 1000);
		assertEquals(420, focus.x);
		assertEquals(200, focus.y);
	}

	public void testFocusStaysInsideThePicture() {
		// a box overflowing the picture must not give a focus outside of it
		Point focus = FaceFocusService.focusPoint(Arrays.asList(new Rectangle(980, -60, 80, 80)), 1000, 1000);
		assertEquals(1000, focus.x);
		assertEquals(0, focus.y);
	}
}
