package org.javlo.component.dynamic;

import java.util.Map;

import org.javlo.template.Template;

import junit.framework.TestCase;

/**
 * parseConfigComment lit la définition d'un composant dynamique dans le
 * commentaire <!--config --> du renderer. Le format est celui d'un fichier
 * properties : une déclaration par ligne, et la valeur peut contenir des
 * espaces (un libellé est une phrase). Un découpage sur les espaces collait les
 * déclarations entre elles et tronquait toutes les valeurs au premier espace.
 */
public class DynamicComponentConfigCommentTest extends TestCase {

	private static final String CONFIG_BLOCK = "<!--config\n" //
			+ "component.type=header_slide\n" //
			+ "component.label=Hero / Header slide - empilable comme un swiper\n" //
			+ "component.css-class=header-slide-component\n" //
			+ "component.renderer=/components/header_slide.html\n" //
			+ "\n" //
			+ "image.reference.type=image\n" //
			+ "image.reference.label=Image de fond\n" //
			+ "image.reference.order=100\n" //
			+ "\n" //
			+ "link.reference.label=Bouton CTA (optionnel)\n" //
			+ "link.reference.order=400\n" //
			+ "-->";

	public void testValueCanContainSpaces() {
		Map<String, String> config = DynamicComponent.parseConfigComment(CONFIG_BLOCK);
		assertNotNull(config);
		assertEquals("Hero / Header slide - empilable comme un swiper", config.get("component.label"));
		assertEquals("Image de fond", config.get("image.reference.label"));
		assertEquals("Bouton CTA (optionnel)", config.get("link.reference.label"));
	}

	public void testSimpleValuesAreIntact() {
		Map<String, String> config = DynamicComponent.parseConfigComment(CONFIG_BLOCK);
		assertEquals("header_slide", config.get("component.type"));
		assertEquals("/components/header_slide.html", config.get("component.renderer"));
		assertEquals("100", config.get("image.reference.order"));
		assertEquals("400", config.get("link.reference.order"));
	}

	public void testNoParasiteKeyIsCreated() {
		Map<String, String> config = DynamicComponent.parseConfigComment(CONFIG_BLOCK);
		assertEquals(9, config.size());
		for (String key : config.keySet()) {
			assertFalse("clé parasite : " + key, key.contains(" "));
			assertFalse("clé parasite : " + key, key.contains("("));
		}
		for (Map.Entry<String, String> entry : config.entrySet()) {
			assertFalse("valeur collée sur " + entry.getKey() + " : " + entry.getValue(),
					entry.getValue().contains("=") && entry.getValue().contains("reference"));
		}
	}

	public void testBlankLinesAndCommentsAreIgnored() {
		Map<String, String> config = DynamicComponent.parseConfigComment("<!--config\n" //
				+ "# un commentaire\n" //
				+ "\n" //
				+ "component.type=intro\n" //
				+ "   \n" //
				+ "-->");
		assertEquals(1, config.size());
		assertEquals("intro", config.get("component.type"));
	}

	public void testNoConfigComment() {
		assertNull(DynamicComponent.parseConfigComment("<div>rien à voir</div>"));
	}

	/**
	 * La marque du générateur sert à reconstruire les .jsp produits par une version
	 * antérieure : elle doit survivre à la minification, sinon chaque rendu
	 * régénère le fichier.
	 */
	public void testGeneratorStampSurvivesMinifyContent() {
		String jsp = Template.minifyContent(DynamicComponent.JSP_HEADER + DynamicComponent.GENERATOR_STAMP + CONFIG_BLOCK + "\n<div>x</div>");
		assertTrue("marque perdue : " + jsp, jsp.contains(DynamicComponent.GENERATOR_STAMP));
	}

	/**
	 * Le cas réel : le renderer est minifié avant d'être écrit en .jsp, et c'est
	 * ce .jsp qui est relu pour en extraire la définition.
	 */
	public void testRoundTripThroughMinifyContent() {
		String jsp = Template.minifyContent(CONFIG_BLOCK + "\n<div>\n   <span>x</span>\n</div>");
		Map<String, String> config = DynamicComponent.parseConfigComment(jsp);
		assertNotNull(config);
		assertEquals("header_slide", config.get("component.type"));
		assertEquals("Hero / Header slide - empilable comme un swiper", config.get("component.label"));
		assertEquals("header-slide-component", config.get("component.css-class"));
		assertEquals("Image de fond", config.get("image.reference.label"));
		assertEquals("100", config.get("image.reference.order"));
		assertEquals("Bouton CTA (optionnel)", config.get("link.reference.label"));
		assertEquals("400", config.get("link.reference.order"));
		assertNull(config.get("swipercomponent.css-class"));
		assertNull(config.get("fondimage.reference.order"));
	}
}
