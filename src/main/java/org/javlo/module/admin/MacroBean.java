package org.javlo.module.admin;

import org.javlo.macro.core.IMacro;

public class MacroBean {
	
	private String name;
	private String info;
	private String modalSize;
	
	public MacroBean(String name, String info, String modalSize) {
		super();
		this.name = name;
		this.info = info;
		this.modalSize = modalSize;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	@Override
	public String toString() {	
		return name;
	}
	public String getModalSize() {
		if (modalSize == null) {
			return IMacro.DEFAULT_MAX_MODAL_SIZE;
		}
		return modalSize;
	}
	public void setModalSize(String modalSize) {
		this.modalSize = modalSize;
	}

}
