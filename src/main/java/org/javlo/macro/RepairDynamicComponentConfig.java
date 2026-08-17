package org.javlo.macro;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.component.meta.MetaComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.utils.StructuredProperties;

/**
 * Nettoie les composants dynamiques abîmés par l'ancienne lecture du bloc
 * <!--config --> du renderer : la minification du JSP généré supprimait les
 * retours ligne du bloc, et parseConfigComment découpait ensuite sur les espaces.
 * <p>
 * Deux dégâts à réparer :
 * <ul>
 * <li>les propriétés du composant, avec des clés bâties sur la fin du libellé
 * précédent (<code>swipercomponent.css-class</code>,
 * <code>(optionnel)link.reference.order</code>) et des valeurs sur lesquelles la
 * déclaration suivante est collée ;</li>
 * <li>le <em>type</em> du composant. <code>component.type</code> valant
 * <code>header_slidecomponent.label=Hero</code>, c'est cette chaîne que
 * MacroHelper a recopiée dans le type des composants créés par
 * import-default-language : ils s'affichent en «&nbsp;unknow component&nbsp;».
 * Le vrai type est retrouvé en cherchant, parmi les types connus du site, celui
 * qui préfixe la chaîne abîmée.</li>
 * </ul>
 * Le contenu saisi n'est jamais touché : tout ce qui commence par
 * <code>field.</code> est conservé tel quel. Les clés de définition supprimées
 * sont réinjectées depuis le template au rendu suivant.
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

		int propsCount = 0;
		int keyCount = 0;
		int typeCount = 0;

		for (String lg : globalContext.getContentLanguages()) {
			ContentContext ctxLg = new ContentContext(ctx);
			ctxLg.setLanguage(lg);
			ctxLg.setContentLanguage(lg);
			ctxLg.setRequestContentLanguage(lg);
			ctxLg.setArea(null);

			for (MenuElement page : content.getNavigation(ctxLg).getAllChildrenList()) {
				Set<String> knownTypes = getKnownTypes(ctxLg, page);
				ContentElementList comps = page.getContent(ctxLg);
				while (comps.hasNext(ctxLg)) {
					IContentVisualComponent comp = comps.next(ctxLg);
					ComponentBean bean = comp.getComponentBean();
					if (bean == null) {
						continue;
					}
					boolean modified = false;

					/* 1. le type, sinon le composant reste un «unknow component» */
					String repairedType = repairType(bean.getType(), knownTypes);
					if (repairedType != null) {
						logger.info("repair component type '" + bean.getType() + "' -> '" + repairedType + "' (" + page.getPath() + " / " + lg + ')');
						bean.setType(repairedType);
						typeCount++;
						modified = true;
					}

					/* 2. les propriétés */
					if (comp instanceof DynamicComponent && !(comp instanceof MetaComponent)) {
						DynamicComponent dynComp = (DynamicComponent) comp;
						Properties props = dynComp.getProperties();
						if (props != null && isCorrupted(props)) {
							int removed = removeDefinitionKeys(props);
							dynComp.storeProperties();
							propsCount++;
							keyCount += removed;
							modified = true;
							logger.info("repair dynamic component " + bean.getType() + " (" + page.getPath() + " / " + lg + ") : " + removed + " definition key(s) removed.");
						}
					} else if (repairedType != null) {
						/* chargé comme Unknown : ses propriétés ne sont pas montées, on nettoie la valeur brute */
						int removed = repairRawValue(bean);
						if (removed > 0) {
							propsCount++;
							keyCount += removed;
							logger.info("repair raw value of " + bean.getType() + " (" + page.getPath() + " / " + lg + ") : " + removed + " definition key(s) removed.");
						}
					}

					if (modified) {
						bean.setModify(true);
					}
				}
			}
		}

		PersistenceService.getInstance(globalContext).setAskStore(true);

		return typeCount + " component type(s) repaired, " + propsCount + " component(s) cleaned (" + keyCount + " definition key(s) removed). The definition is rebuilt from the template on next rendering.";
	}

	/** tous les types de composants disponibles pour cette page, template compris. */
	private Set<String> getKnownTypes(ContentContext ctx, MenuElement page) throws Exception {
		Set<String> types = new HashSet<String>();
		for (IContentVisualComponent comp : ComponentFactory.getComponents(ctx, page)) {
			if (comp != null && comp.getType() != null) {
				types.add(comp.getType());
			}
		}
		return types;
	}

	/**
	 * Un type abîmé est un type inconnu qui commence par un type connu, le reste
	 * étant la déclaration collée. Le type connu le plus long gagne, et on ne
	 * renvoie rien si le type est déjà valide ou si aucun candidat ne correspond :
	 * mieux vaut un «unknow component» visible qu'un type deviné.
	 */
	static String repairType(String type, Set<String> knownTypes) {
		if (type == null || knownTypes.contains(type) || !GLUED_DECLARATION.matcher(type).find()) {
			return null;
		}
		String best = null;
		for (String known : knownTypes) {
			if (type.startsWith(known) && type.length() > known.length()) {
				Matcher glue = GLUED_DECLARATION.matcher(type.substring(known.length()));
				if (glue.find() && glue.start() == 0 && (best == null || known.length() > best.length())) {
					best = known;
				}
			}
		}
		return best;
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

	/**
	 * Nettoyage de la valeur persistée sans passer par l'instance : le composant a
	 * été chargé comme Unknown, faute d'un type exploitable.
	 */
	static int repairRawValue(ComponentBean bean) {
		String value = bean.getValue();
		if (value == null || value.trim().isEmpty()) {
			return 0;
		}
		try {
			StructuredProperties props = new StructuredProperties(true);
			props.load(new ByteArrayInputStream(value.getBytes(ContentContext.CHARACTER_ENCODING)));
			if (!isCorrupted(props)) {
				return 0;
			}
			int removed = removeDefinitionKeys(props);
			bean.setValue(props.toString());
			return removed;
		} catch (Exception e) {
			logger.warning("can't repair raw value of " + bean.getType() + " : " + e.getMessage());
			return 0;
		}
	}

	@Override
	public boolean isPreview() {
		return true;
	}
}
