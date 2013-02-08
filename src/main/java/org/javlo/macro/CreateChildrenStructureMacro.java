package org.javlo.macro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;
import org.javlo.template.Template;

/**
 * <p>
 * create a children page structure under the current page. the structure is defined in the template in macro forder in children-structure.txt. sample file structure :
 * </p>
 * <code>
 * [parent]-general <br />
 * [parent]-technical <br />
 * >[parent]-tech-agri <br />
 * [parent]-[random] <br />
 * <[parent]-financial <br />
 * </code>
 * <ul>
 * <li>> : for create a child of the previous page</li>
 * <li>< : for create a parent of the previous page</li>
 * <li>= : for create a brother of the previous page</li>
 * <li>c: : for content of the previous page. (format:{@link MacroHelper#insertContent(ContentContext, MenuElement, String)})</li>
 * <li>[parent] : replace with the name of the parent.</li>
 * <li>[random] : replace with random id.</li>
 * <li>[root] : the root page (current page when macro is executed.</li>
 * <li>[CR] : carriage return.</li>
 * </ul>
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class CreateChildrenStructureMacro extends AbstractMacro {

	private static Logger logger = Logger.getLogger(CreateChildrenStructureMacro.class.getName());

	private static final String STRUCTURE_FILE_NAME = "children-structure.txt";

	@Override
	public String getName() {
		return "create-children-site-structure-here";
	}

	@Override
	public String perform(ContentContext inCtx, Map<String, Object> params) throws Exception {

		ContentContext localCtx = new ContentContext(inCtx);

		MenuElement currentPage = localCtx.getCurrentPage();

		Template template = localCtx.getCurrentTemplate();
		if (template != null) {
			GlobalContext globalContext = GlobalContext.getInstance(localCtx.getRequest());
			File structureFile = template.getMacroFile(globalContext, STRUCTURE_FILE_NAME);
			if (!structureFile.exists()) {
				String msg = "file not found " + STRUCTURE_FILE_NAME + " in template : " + template.getName();
				MessageRepository.getInstance(localCtx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
				return null;
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(structureFile), ContentContext.CHARACTER_ENCODING));
			MenuElement page = currentPage;
			String line = reader.readLine();
			while (line != null) {
				line = line.trim();
				MenuElement parent = page.getParent();
				if (line.startsWith(">")) {
					parent = page;
				} else if (line.startsWith("<")) {
					if (page == currentPage) {
						String msg = "bad structure in file " + STRUCTURE_FILE_NAME + " you can ask the parent of the current page. (use '<' only after '>').";
						MessageRepository.getInstance(localCtx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
						return null;
					}
					parent = page.getParent().getParent();
				} else if (line.startsWith("=")) {
					parent = page.getParent();
				} else if (line.toLowerCase().startsWith("c:")) {
					line = line.substring(2);
					line = line.replace("[parent]", parent.getName());
					line = line.replace("[random]", StringHelper.getRandomId());
					line = line.replace("[root]", currentPage.getName());
					line = line.replace("[CR]", "\r\n");
					logger.info("create content : " + line);
					MacroHelper.insertContent(localCtx, page, line);
					parent = null;
				} else {
					String msg = "bad structure in file " + STRUCTURE_FILE_NAME + " all lines must start with : '>','<','=' or 'c:'.";
					MessageRepository.getInstance(localCtx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
					return null;
				}
				if (parent != null) {
					line = line.substring(1).trim();
					line = line.replace("[parent]", parent.getName());
					line = line.replace("[random]", StringHelper.getRandomId());
					line = line.replace("[root]", currentPage.getName());
					logger.info("create page : " + line);
					page = MacroHelper.addPageIfNotExist(localCtx, parent, line, false, false);
				}
				line = reader.readLine();
			}
			reader.close();

			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
			persistenceService.store(localCtx);

		}

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

}
