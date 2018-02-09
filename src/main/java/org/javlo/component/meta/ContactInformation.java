package org.javlo.component.meta;

import java.util.Arrays;
import java.util.List;

import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;

public class ContactInformation extends AbstractPropertiesComponent {
	
	private static final List<String> FIELDS = Arrays.asList(new String[] {"name", "addresse", "zip", "city", "country", "web", "phone", "email"});
	
	public static final String TYPE = "contact-information"; 

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}
	
	public ContactBean getContactBean() {
		ContactBean outBean = new ContactBean();
		outBean.setName(getFieldValue("name"));
		outBean.setAddresse(getFieldValue("addresse"));
		outBean.setZip(getFieldValue("zip"));
		outBean.setCity(getFieldValue("city"));
		outBean.setCountry(getFieldValue("country"));
		outBean.setWeb(getFieldValue("web"));
		outBean.setPhone(getFieldValue("phone"));
		outBean.setEmail(getFieldValue("email"));
		return outBean;
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("contact", getContactBean());
	}
	
	@Override
	public String getFontAwesome() {	
		return "info";
	}

}
