package org.javlo.macro.core;

import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;

public class MacroBean implements IMacro, IInteractiveMacro {

	private String modalSize;

	private String name;

	private String info = null;

	private String url = null;

	private String params = null;

	private boolean interactive = true;

	private boolean admin = false;

	private boolean preview = true;

	private boolean add = true;

	private boolean forceURL = false;

	private String icon = "fa fa-cogs";

	private int priority = DEFAULT_PRIORITY;

	public MacroBean() {
	}

	public MacroBean(String name, String params) {
		this.name = name;
		this.params = params;
	}

	public MacroBean(String name, String params, String icon) {
		this.name = name;
		this.params = params;
		this.icon = icon;
	}

	@Override
	public String getRenderer() {
		return null;
	}

	@Override
	public String prepare(ContentContext ctx) {
		return null;
	}

	@Override
	public String getModalSize() {
		return modalSize;
	}

	public void setModalSize(String modalSize) {
		this.modalSize = modalSize;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return null;
	}

	@Override
	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	@Override
	public boolean isPreview() {
		return preview;
	}

	public void setPreview(boolean preview) {
		this.preview = preview;
	}

	@Override
	public boolean isAdd() {
		return add;
	}

	public void setAdd(boolean add) {
		this.add = add;
	}

	@Override
	public boolean isInterative() {
		return interactive;
	}

	public void setInteractive(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void init(ContentContext ctx) {
		if (!forceURL) {
			ContentContext editCtx = new ContentContext(ctx);
			editCtx.setRenderMode(ContentContext.EDIT_MODE);
			url = URLHelper.createURL(editCtx);
			url = URLHelper.addParams(url, params);
		}
	}

	@Override
	public String getInfo(ContentContext ctx) {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	@Override
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Override
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		forceURL = true;
		this.url = url;
	}

	@Override
	public int getPriority() {
		return priority;
	}

}
