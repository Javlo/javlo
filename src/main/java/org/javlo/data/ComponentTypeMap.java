package org.javlo.data;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.module.content.ComponentWrapper;
import org.javlo.service.ContentService;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ComponentTypeMap extends AbstractMap<String, Collection<ComponentWrapper>> {

    private final ContentContext ctx;

    public ComponentTypeMap(ContentContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Collection<ComponentWrapper> get(Object key) {
        try {
            List<IContentVisualComponent> comps = ContentService.getInstance(ctx.getGlobalContext()).getComponentByType(ctx, (String)key);
            if (comps == null || comps.size() == 0) {
                return Collections.emptyList();
            } else {
                Collection<ComponentWrapper> out = new LinkedList<>();
                comps.forEach(c -> {
                    out.add(new ComponentWrapper(ctx, c));
                });
                return out;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Override
    public Set<Entry<String, Collection<ComponentWrapper>>> entrySet() {
        return Collections.EMPTY_SET;
    }
}
