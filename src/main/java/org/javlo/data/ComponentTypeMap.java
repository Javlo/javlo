package org.javlo.data;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.service.ContentService;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class ComponentTypeMap extends AbstractMap<String, Collection<IContentVisualComponent>> {

    private final ContentContext ctx;

    public ComponentTypeMap(ContentContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Collection<IContentVisualComponent> get(Object key) {
        try {
            return ContentService.getInstance(ctx.getGlobalContext()).getComponentByType(ctx, (String)key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Override
    public Set<Entry<String, Collection<IContentVisualComponent>>> entrySet() {
        return Collections.EMPTY_SET;
    }
}
