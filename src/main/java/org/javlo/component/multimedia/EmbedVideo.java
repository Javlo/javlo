package org.javlo.component.multimedia;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;

public class EmbedVideo extends Video implements IAction {

	public static final String TYPE = "embed-video";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	protected boolean canUpload() {
		return false;
	}

	@Override
	protected boolean isEmbedCode() {
		return true;
	}

	@Override
	protected boolean isLink() {
		return false;
	}

	@Override
	protected boolean isMutlimediaResource() {
		return false;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return getEmbedCode();
	}

	@Override
	public String getActionGroupName() {
		return "embed";
	}

	@Override
	public String getURL(ContentContext ctx) {
		String url = "/expcomp/" + getId() + ".html";
		return URLHelper.createStaticURL(ctx, url);
	}

}
