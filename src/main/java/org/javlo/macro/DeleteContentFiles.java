package org.javlo.macro;

import java.io.File;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.PersistenceService;

public class DeleteContentFiles extends AbstractMacro {

	@Override
	public String getName() {
		return "delete-content-files";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		PersistenceService persistenceService = PersistenceService.getInstance(ctx.getGlobalContext());
		File persistenceDir = new File(persistenceService.getDirectory());
		int c = 0;
		for (File f : persistenceDir.listFiles()) {
			if (f.getName().startsWith(PersistenceService.STORE_FILE_PREFIX+ContentContext.PREVIEW_MODE)) {
				if (!f.getName().equals(PersistenceService.STORE_FILE_PREFIX+ContentContext.PREVIEW_MODE+ '_'+persistenceService.getVersion() + ".xml")) {
					f.delete();
					c++;
				}
			}
		}
		MessageRepository messageRepository = MessageRepository.getInstance(ctx);
		messageRepository.setGlobalMessage(new GenericMessage("files deleted : "+c, GenericMessage.INFO));
		return null;
	}

	@Override
	public boolean isPreview() {
		return false;
	}

	@Override
	public boolean isAdmin() {
		return true;
	}
};
