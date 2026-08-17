package org.javlo.macro;

import java.util.Properties;

import junit.framework.TestCase;

/**
 * Le jeu de clés utilisé ici est celui relevé sur un composant header_slide
 * réellement abîmé : la définition lue dans le bloc <!--config --> du JSP minifié
 * avait perdu ses retours ligne.
 */
public class RepairDynamicComponentConfigTest extends TestCase {

	private static Properties corrupted() {
		Properties props = new Properties();
		/* contenu saisi : à conserver intégralement */
		props.setProperty("field.title.value", "La précision en standard.");
		props.setProperty("field.title.type", "h2");
		props.setProperty("field.image.value.file", "cover-1.jpg");
		props.setProperty("field.image.value.folder", "import/home");
		props.setProperty("_dynamic_id", "17798314342894838");
		props.setProperty("notify.creation", "false");
		/* définition saine */
		props.setProperty("component.renderer", "/components/header_slide.html");
		props.setProperty("component.label", "Hero");
		props.setProperty("font-awesome", "image");
		/* définition abîmée : valeur avec la déclaration suivante collée */
		props.setProperty("component.type", "header_slidecomponent.label=Hero");
		props.setProperty("fondimage.reference.image.filter", "hero-bannerimage.reference.order=100");
		props.setProperty("swipercomponent.css-class", "header-slide-componentcomponent.renderer=/components/header_slide.html");
		/* définition abîmée : clé bâtie sur la fin du libellé précédent */
		props.setProperty("(optionnel)link.reference.order", "400");
		props.setProperty("d'introductiondescription.reference.order", "300");
		props.setProperty("span.h-orange)title.reference.order", "200");
		return props;
	}

	public void testCorruptionIsDetected() {
		assertTrue(RepairDynamicComponentConfig.isCorrupted(corrupted()));
	}

	public void testHealthyComponentIsLeftAlone() {
		Properties props = new Properties();
		props.setProperty("component.type", "header_slide");
		props.setProperty("component.label", "Hero / Header slide - empilable comme un swiper");
		props.setProperty("component.renderer", "/jsp/components/page-reference/4cols.jsp?colsize=2&noimage=true");
		props.setProperty("font-awesome", "image");
		props.setProperty("image.reference.label", "Image de fond");
		props.setProperty("field.title.value", "Un titre avec un = dedans : a.b=c");
		assertFalse(RepairDynamicComponentConfig.isCorrupted(props));
	}

	public void testContentIsPreservedAndDefinitionDropped() {
		Properties props = corrupted();
		int removed = RepairDynamicComponentConfig.removeDefinitionKeys(props);

		assertEquals(9, removed);
		assertEquals(6, props.size());
		assertEquals("La précision en standard.", props.getProperty("field.title.value"));
		assertEquals("h2", props.getProperty("field.title.type"));
		assertEquals("cover-1.jpg", props.getProperty("field.image.value.file"));
		assertEquals("import/home", props.getProperty("field.image.value.folder"));
		assertEquals("17798314342894838", props.getProperty("_dynamic_id"));
		assertEquals("false", props.getProperty("notify.creation"));

		for (String key : props.stringPropertyNames()) {
			assertTrue("clé de définition restante : " + key, key.startsWith("field.") || key.equals("_dynamic_id") || key.equals("notify.creation"));
		}
	}

	public void testRepairIsIdempotent() {
		Properties props = corrupted();
		RepairDynamicComponentConfig.removeDefinitionKeys(props);
		assertFalse(RepairDynamicComponentConfig.isCorrupted(props));
	}
}
