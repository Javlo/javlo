package org.javlo.module.content;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.fields.Field;
import org.javlo.helper.URLHelper;
import org.javlo.template.Template;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ComponentWrapper {
    private ContentContext ctx;
    private IContentVisualComponent comp;
    private boolean selected;
    private String hexColor;

    public ComponentWrapper(ContentContext ctx, IContentVisualComponent comp) {
        this.ctx = ctx;
        this.comp = comp;
        hexColor = comp.getHexColor();
    }

    public String getType() {
        return comp.getType();
    }

    public String getLabel() {
        return comp.getComponentLabel(ctx, ctx.getGlobalContext().getEditLanguage(ctx.getRequest().getSession()));
    }

    public String getValue() {
        return comp.getValue(ctx);
    }

    public boolean isMetaTitle() {
        return comp.isMetaTitle();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getComplexityLevel() {
        return comp.getComplexityLevel(ctx);
    }

    public boolean isDynamicComponent() {
        return comp instanceof DynamicComponent;
    }

    public String getHexColor() {
        return hexColor;
    }

    public void setHexColor(String hexColor) {
        this.hexColor = hexColor;
    }

    public IContentVisualComponent getComponent() {
        return comp;
    }

    public String getFontAwesome() {
        return comp.getFontAwesome();
    }

    public String getIcon() {
        return comp.getIcon();
    }

    /**
     * Folder, inside the current template, where optional component preview images are stored.
     * A preview is named after the component type : <code>visual/components/&lt;type&gt;.png</code>.
     */
    public static final String VISUAL_PREVIEW_FOLDER = "visual/components";

    private boolean visualPreviewURLComputed = false;
    private String visualPreviewURL = null;

    /**
     * URL of the preview image defined in the current template for this component, or
     * <code>null</code> when no such image exists. When defined, the image is meant to be
     * displayed inside the component box of the palette, with the component name on top of it.
     */
    public String getVisualPreviewURL() {
        if (!visualPreviewURLComputed) {
            visualPreviewURLComputed = true;
            try {
                Template template = ctx.getCurrentTemplate();
                if (template != null) {
                    GlobalContext globalContext = ctx.getGlobalContext();
                    String relativePath = VISUAL_PREVIEW_FOLDER + "/" + getType() + ".png";
                    File previewFile = new File(URLHelper.mergePath(template.getWorkTemplateRealPath(globalContext), relativePath));
                    if (previewFile.exists()) {
                        visualPreviewURL = URLHelper.createStaticTemplateURL(ctx, template, relativePath);
                    }
                }
            } catch (Exception e) {
                visualPreviewURL = null;
            }
        }
        return visualPreviewURL;
    }

    public String getGroup() {
        return comp.getGroup();
    }

    public String getXHTMLCode() throws Exception {
        return comp.getXHTMLCode(ctx);
    }


    private Map<String, Field.FieldBean> fields = null;
    public Map<String, Field.FieldBean> getFields() throws Exception {
        if (fields == null) {
            if (!isDynamicComponent()) {
                fields = Collections.emptyMap();
            } else {
                DynamicComponent dc = (DynamicComponent) comp;
                fields = new HashMap<>();
                dc.getFields(ctx).forEach(f -> {
                    fields.put(f.getName(), f.getBean(ctx));
                });
            }
        }
        return fields;
    }


}
