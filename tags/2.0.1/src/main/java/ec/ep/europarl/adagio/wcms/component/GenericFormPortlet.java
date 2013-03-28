package ec.ep.europarl.adagio.wcms.component;


public class GenericFormPortlet extends AbstractPortletComponent {

	@Override
	public String getType() {
		return "generic-form-portlet";
	}

	@Override
	public String getPortletName() {
		return "GenericForm";
	}
}
