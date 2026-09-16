package org.javlo.filter;

import junit.framework.TestCase;

public class CatchAllFilterWorkTemplatePropertiesTest extends TestCase {

	public void testPropertiesInWorkTemplateBlocked() {
		assertTrue(CatchAllFilter.isWorkTemplateProperties("/belrobotics/wktp/belrobotics/belrobotics/staging.properties"));
		assertTrue(CatchAllFilter.isWorkTemplateProperties("/wktp/belrobotics/belrobotics/config.properties"));
		assertTrue(CatchAllFilter.isWorkTemplateProperties("/javlo2/sexy/wktp/andromede/sexy/i18n/view_fr.properties"));
	}

	public void testBypassVariantsBlocked() {
		assertTrue(CatchAllFilter.isWorkTemplateProperties("/wktp/tpl/ctx/CONFIG.Properties"));
		assertTrue(CatchAllFilter.isWorkTemplateProperties("/wktp/tpl/ctx/config.properties;jsessionid=123"));
		assertTrue(CatchAllFilter.isWorkTemplateProperties("/wktp/tpl/ctx/config.properties."));
		assertTrue(CatchAllFilter.isWorkTemplateProperties("/wktp/tpl/ctx/config.properties "));
		assertTrue(CatchAllFilter.isWorkTemplateProperties("\\wktp\\tpl\\ctx\\config.properties"));
	}

	public void testOtherFilesAllowed() {
		assertFalse(CatchAllFilter.isWorkTemplateProperties("/belrobotics/wktp/belrobotics/belrobotics/style.css"));
		assertFalse(CatchAllFilter.isWorkTemplateProperties("/belrobotics/wktp/belrobotics/belrobotics/index.html"));
		assertFalse(CatchAllFilter.isWorkTemplateProperties("/wktp/tpl/ctx/properties/logo.png"));
	}

	public void testPropertiesOutsideWorkTemplateUntouched() {
		assertFalse(CatchAllFilter.isWorkTemplateProperties("/belrobotics/static/config.properties"));
		assertFalse(CatchAllFilter.isWorkTemplateProperties("/wktp.properties"));
	}
}
