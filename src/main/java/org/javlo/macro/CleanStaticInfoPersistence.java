package org.javlo.macro;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;
import org.javlo.helper.LocalLogger;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.ztatic.StaticInfo;

/**
 * clean static info storage, remove meta data if resource not found and remove
 * reference to defaut value
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class CleanStaticInfoPersistence extends AbstractMacro {

	private static Logger logger = Logger.getLogger(CleanStaticInfoPersistence.class.getName());

	@Override
	public String getName() {
		return "clean-static-info";
	}

	@Override
	public String perform(ContentContext inCtx, Map<String, Object> params) throws Exception {
		ContentService content = ContentService.getInstance(inCtx.getGlobalContext());
		Collection<Object> willBeRemoved = new LinkedList<Object>();
		for (Object key : content.getPreviewKeys()) {
			if (StaticInfo.isStaticInfoKey(key)) {

				String url = StaticInfo.getStaticUrlFromKey(key);
				if (url != null) {
					StaticInfo staticInfo = StaticInfo.getInstance(inCtx, url);
					LocalLogger.log(">>>>>>>>> CleanStaticInfo.perform : key STATIC INFO key = " + key); // TODO: remove debug trace
					LocalLogger.log(">>>>>>>>> CleanStaticInfo.perform : key STATIC INFO value = " + content.getPreviewAttribute("" + key)); // TODO: remove debug trace
					LocalLogger.log(">>>>>>>>> CleanStaticInfo.perform : staticInfo = " + staticInfo.getFile().getAbsolutePath()); // TODO: remove debug trace
					LocalLogger.log(">>>>>>>>> CleanStaticInfo.perform : file exist = " + staticInfo.getFile().exists()); // TODO: remove debug trace
					LocalLogger.log("");
					if (!staticInfo.getFile().exists()) {
						willBeRemoved.add(key);
					} else {
						if (StaticInfo.isDefaultStaticKeyValue(key, content.getPreviewAttribute("" + key))) {
							willBeRemoved.add(key);
						}
					}
				}
				// System.out.println(">>>>>>>>> CleanStaticInfo.perform :
				// ----------------------------------------------------------------"); //TODO:
				// remove debug trace
			}
		}
		content.removePreviewAttributes(willBeRemoved);
		PersistenceService.getInstance(inCtx.getGlobalContext()).setAskStore(true);
		return "#keys deleted : " + willBeRemoved.size();
	}

	@Override
	public boolean isPreview() {
		return true;
	}

	public static void main(String[] args) {
		String key = "staticinfo-/files/dossier-de-presse-toni-erdmann.pdf-shared-x";
		System.out.println(StaticInfo.getStaticUrlFromKey(key));
	}

}
