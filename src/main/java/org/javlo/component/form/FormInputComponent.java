package org.javlo.component.form;

import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.utils.ListMapValueValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FormInputComponent extends AbstractPropertiesComponent {

    private static final String TYPE = "form-input";

    private static final String FIELD_TYPE = "type";

    private static final List<String> FIELDS = Arrays.asList(new String[] {FIELD_TYPE, "name", "legend", "description", "descriptionLarge", "tips"});

    private static final Map<String,String> INPUT_TYPE = new ListMapValueValue<>(Arrays.asList(new String[] {"text", "number", "radio", "checkbox"}));

    @Override
    public Collection<Map.Entry<String, String>> getFieldChoice(ContentContext ctx, String fieldName) {
        if (fieldName.equals(FIELD_TYPE)) {
             return INPUT_TYPE.entrySet();
        }
        return super.getFieldChoice(ctx, fieldName);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public List<String> getFields(ContentContext ctx) throws Exception {
        return FIELDS;
    }

    @Override
    public String getIcon() {
        return "bi bi-input-cursor-text";
    }
}
