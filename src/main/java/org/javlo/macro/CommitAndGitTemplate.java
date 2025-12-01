package org.javlo.macro;

import org.javlo.context.ContentContext;

import java.util.Map;

public class CommitAndGitTemplate extends AbstractMacro {

    @Override
    public String getName() {
        return "git-co-and-commit-template";
    }

    @Override
    public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
        ctx.getCurrentTemplate().clearRendererAndGit(ctx);
        return null;
    }

    @Override
    public boolean isPreview() {
        return true;
    }

}
