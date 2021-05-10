package org.javlo.component.web2.survey;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.utils.Cell;
import org.javlo.utils.XLSTools;

public class DisplayResult extends AbstractSurvey implements IAction {

	protected static final String TITLE_FIELD = "title";
	protected static final String FILE_NAME = "file";
	protected static final String LENCIONI = "lencioni";

	public static final String TYPE = "display-result";

	@Override
	public String getType() {
		return TYPE;
	}

	private static final List<String> FIELDS = Arrays.asList(new String[] { TITLE_FIELD, FILE_NAME });

	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		boolean out = super.initContent(ctx);
		return out;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("title", getFieldValue(TITLE_FIELD));

		String filePath = getFieldValue(FILE_NAME);
		Cell[][] cells;
		if (filePath.toLowerCase().startsWith("http")) {
			cells = XLSTools.getArray(null, new URL(filePath));
		} else {
			File file = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), filePath));
			if (!file.exists()) {
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				messageRepository.setGlobalMessage(new GenericMessage("file not found : " + file, GenericMessage.ERROR));
				return;
			} else {
				cells = XLSTools.getArray(null, file);
			}
		}
		if (getCurrentRenderer(ctx).contains(LENCIONI)) {
			Map<String, Double> average = SurveyAverage.average(cells);
			ctx.getRequest().setAttribute("average", average);
		}

	}

	@Override
	public String getActionGroupName() {
		return TYPE;
	}

}
