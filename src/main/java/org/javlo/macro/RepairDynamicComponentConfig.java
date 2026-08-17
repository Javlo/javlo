package org.javlo.macro;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.component.meta.MetaComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;

/**
 * Nettoie les propriétés des composants dynamiques polluées par l'ancienne
 * lecture du bloc <!--config --> du renderer : la minification du JSP généré
 * supprimait les retours ligne du bloc, et parseConfigComment découpait ensuite
 * sur les espaces. Résultat, des clés absurdes bâties sur la fin du libellé
 * précédent (<code>swipercomponent.css-class</code>,
 * <code>fondimage.reference.image.filter</code>,
 * <code>(optionnel)link.reference.order</code>) et des valeurs sur lesquelles la
 * déclaration suivante était collée (<code>component.type</code> valant
 * <code>header_slidecomponent.label=Hero</code>).
 * <p>
 * Le contenu saisi n'est jamais touché : tout ce qui commence par
 * <code>field.</code> est conservé tel quel. Seules les clés de définition sont
 * supprimées, et elles sont réinjectées depuis le template au rendu suivant
 * (c'est exactement ce que fait DynamicComponent à chaque affichage).
 *
 * @author pvandermaesen
 */
public class RepairDynamicComponentConfig extends AbstractMacro {

	private static Logger logger = Logger.getLogger(RepairDynamicComponentConfig.class.getName());

	/** contenu saisi : préfixe des clés à conserver quoi qu'il arrive. */
	private static final String CONTENT_PREFIX = "field.";

	/** clés hors définition écrites par le code à l'exécution. */
	private static final List<String> RUNTIME_KEYS = new LinkedList<String>(java.util.Arrays.asList("_dynamic_id", "notify.creation"));

	/** un caractère hors de ce jeu dans une clé de définition signe la corruption. */
	private static final Pattern LEGAL_KEY = Pattern.compile("[A-Za-z0-9_.\\-\\[\\]]+");

	/**
	 * une déclaration collée à la fin de la valeur précédente : une clé pointée
	 * suivie de son <code>=</code>. Un simple <code>=</code> ne suffit pas, une URL
	 * de renderer en contient légitimement (<code>4cols.jsp?colsize=2</code>).
	 */
	private static final Pattern GLUED_DECLARATION = Pattern.compile("[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+=");

	@Override
	public String getName() {
		return "repair-dynamic-component-config";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		ContentService content = ContentService.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		int compCount = 0;
		int keyCount = 0;

		for (String lg : globalContext.getContentLanguages()) {
			ContentContext ctxLg = new ContentContext(ctx);
			ctxLg.setLanguage(lg);
			ctxLg.setContentLanguage(lg);
			ctxLg.setRequestContentLanguage(lg);
			ctxLg.setArea(null);

			for (MenuElement page : content.getNavigation(ctxLg).getAllChildrenList()) {
				ContentElementList comps = page.getContent(ctxLg);
				while (comps.hasNext(ctxLg)) {
					IContentVisualComponent comp = comps.next(ctxLg);
					if (!(comp instanceof DynamicComponent) || comp instanceof MetaComponent) {
						continue;
					}
					DynamicComponent dynComp = (DynamicComponent) comp;
					Properties props = dynComp.getProperties();
					if (props == null || !isCorrupted(props)) {
						continue;
					}
					int removed = removeDefinitionKeys(props);
					compCount++;
					keyCount += removed;
					logger.info("repair dynamic component " + dynComp.getType() + " (" + page.getPath() + " / " + lg + ") : " + removed + " definition key(s) removed.");
					dynComp.storeProperties();
					dynComp.setModify();
				}
			}
		}

		PersistenceService.getInstance(globalContext).setAskStore(true);

		return compCount + " corrupted component(s) cleaned, " + keyCount + " definition key(s) removed. The definition is rebuilt from the template on next rendering.";
	}

	/**
	 * Un composant est corrompu si une de ses clés de définition porte un caractère
	 * illégal, ou si sa valeur contient la déclaration suivante collée à sa fin.
	 */
	static boolean isCorrupted(Properties props) {
		for (String key : props.stringPropertyNames()) {
			if (key.startsWith(CONTENT_PREFIX) || RUNTIME_KEYS.contains(key)) {
				continue;
			}
			if (!LEGAL_KEY.matcher(key).matches()) {
				return true;
			}
			String value = props.getProperty(key);
			if (value != null && GLUED_DECLARATION.matcher(value).find()) {
				return true;
			}
		}
		return false;
	}

	/** supprime toutes les clés de définition : le template les réécrira. */
	static int removeDefinitionKeys(Properties props) {
		int outCount = 0;
		Iterator<Object> keys = props.keySet().iterator();
		while (keys.hasNext()) {
			String key = "" + keys.next();
			if (!key.startsWith(CONTENT_PREFIX) && !RUNTIME_KEYS.contains(key)) {
				keys.remove();
				outCount++;
			}
		}
		return outCount;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
}
