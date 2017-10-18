package org.javlo.macro;

import java.util.Map;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.files.GenericFile;
import org.javlo.context.ContentContext;

public class FileInListMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "file-in-list";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		ContentContext areaNoArea = ctx.getContextWithArea(null);

		ContentElementList compList = ctx.getCurrentPage().getContent(areaNoArea);
		while (compList.hasNext(areaNoArea)) {
			IContentVisualComponent comp = compList.next(areaNoArea);
			if (comp instanceof GenericFile) {
				comp.setList(true);
			}
		}

		return null;
	}

	@Override
	public boolean isAdmin() {
		return true;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
};
