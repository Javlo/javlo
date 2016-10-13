package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;

public class CleanImportFolder extends AbstractMacro {

	@Override
	public String getName() {
		return "clean-import-folder";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		int deleted = ResourceHelper.cleanImportResources(ctx);
		MessageRepository messageRepository = MessageRepository.getInstance(ctx);
		messageRepository.setGlobalMessage(new GenericMessage("folder deleted : "+deleted, GenericMessage.INFO));
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

};
