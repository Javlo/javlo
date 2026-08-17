package org.javlo.template;

import junit.framework.TestCase;

/**
 * Le bloc <!--config ... --> d'un renderer de composant dynamique est la
 * définition du composant : une déclaration <code>clé=valeur</code> par ligne.
 * La minification du renderer ne doit donc pas y toucher, sinon les
 * déclarations se retrouvent collées entre elles et
 * DynamicComponent.parseConfigComment fabrique des clés absurdes du genre
 * <code>swipercomponent.css-class</code>.
 */
public class MinifyContentConfigTest extends TestCase {

	private static final String CONFIG_BLOCK = "<!--config\n" //
			+ "component.type=header_slide\n" //
			+ "component.label=Hero / Header slide - empilable comme un swiper\n" //
			+ "component.css-class=header-slide-component\n" //
			+ "\n" //
			+ "image.reference.type=image\n" //
			+ "image.reference.label=Image de fond\n" //
			+ "image.reference.order=100\n" //
			+ "-->";

	public void testConfigBlockIsKeptVerbatim() {
		String out = Template.minifyContent(CONFIG_BLOCK + "\n<div>\n    <span>x</span>\n</div>");
		assertTrue("le bloc config doit être conservé tel quel, obtenu : " + out, out.contains(CONFIG_BLOCK));
	}

	public void testNoDeclarationIsGluedToTheNextOne() {
		String out = Template.minifyContent(CONFIG_BLOCK + "\n<div>x</div>");
		assertFalse("type et label collés : " + out, out.contains("header_slidecomponent.label"));
		assertFalse("label et css-class collés : " + out, out.contains("swipercomponent.css-class"));
		assertFalse("label et order collés : " + out, out.contains("fondimage.reference.order"));
	}

	public void testSeveralConfigBlocksAreKept() {
		String second = "<!--config\ncomponent.type=other\n-->";
		String out = Template.minifyContent(CONFIG_BLOCK + "\n<div>x</div>\n" + second);
		assertTrue(out.contains(CONFIG_BLOCK));
		assertTrue(out.contains(second));
	}

	public void testHtmlIsStillMinified() {
		assertEquals("<div><span>a</span></div>", Template.minifyContent("<div>\n    <span>a</span>\n</div>"));
	}

	public void testRegularCommentsAreStillRemoved() {
		assertEquals("<div></div>", Template.minifyContent("<div><!-- un commentaire\nsur 2 lignes --></div>"));
	}

	public void testRegularCommentAroundAConfigBlock() {
		String out = Template.minifyContent("<!-- bla -->" + CONFIG_BLOCK + "<!-- bli -->");
		assertTrue(out.contains(CONFIG_BLOCK));
		assertFalse(out.contains("bla"));
		assertFalse(out.contains("bli"));
	}
}
