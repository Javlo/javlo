package org.javlo.data.rest;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.user.AdminUserSecurity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class RestPageHistory implements IRestFactory {

    Logger logger = Logger.getLogger(RestPageHistory.class.getName());

    @Override
    public String getName() {
        return "pagehistory";
    }

    @Override
    public IRestItem search(ContentContext ctx, String path, String query, int max) throws Exception {

        ContentService content = ContentService.getInstance(ctx.getRequest());
        ContentContext previewCtx = ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE);

        MenuElement previousVersion = content.getNavigation(previewCtx).searchChildFromId(path);
        if (previousVersion == null) {
            logger.warning("page not found : " + path);
            return null;
        }

        if (!AdminUserSecurity.canModifyPage(ctx, previousVersion, false)) {
            logger.warning("user not authorized to see history : " + path + " user : " + ctx.getCurrentUserId());
            return null;
        }

        Map<String, Object> out = new LinkedHashMap<>();
        String pageInfo = previousVersion.getLatestEditor() + " | #comp=" + previousVersion.getContent().length + " | path=" + previousVersion.getPath();
        out.put(StringHelper.renderSortableTime(previousVersion.getModificationDate(previewCtx)), pageInfo);

        PersistenceService ps = PersistenceService.getInstance(ctx.getGlobalContext());
        int version = ps.getVersion();
        while (ps.isPreviewVersion(version) && previousVersion != null) {
            MenuElement root = ps.loadPreview(ctx, version);
            MenuElement page = root.searchChildFromId(path);
            if (page != null) {
                if (!page.equals(previousVersion)) {
                    pageInfo = page.getLatestEditor() + " | #comp=" + page.getContent().length + " | path=" + page.getPath();
                    out.put(StringHelper.renderSortableTime(page.getModificationDate(previewCtx)), pageInfo);
                    if (out.size() >= max) {
                        break;
                    }
                }
            }
            previousVersion = page;
            version--;
        }
        return new RestItemBean(out);
    }
}
