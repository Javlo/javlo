package org.javlo.module.admin;

import org.javlo.macro.core.IMacro;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MacroBean {
	
	private String name;
	private String info;
	private String modalSize;
	private int priority = IMacro.DEFAULT_PRIORITY;
	
	public MacroBean(String name, String info, String modalSize, int priority) {
		super();
		this.name = name;
		this.info = info;
		this.modalSize = modalSize;
		this.priority = priority;
	}
	public String getLabel() {
		if (name == null || name.length() < 3) {
			return name;
		}
		String label = name.replace("macro.", "").replace("-", " ");
		label = label.substring(0, 1).toUpperCase() + label.substring(1);
		return label;
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
	
	public int getPriority() {
		return priority;
	}
	
	public static void sort(List<org.javlo.module.admin.MacroBean> macros) {
		Collections.sort(macros, new Comparator<org.javlo.module.admin.MacroBean>() {
			@Override
			public int compare(org.javlo.module.admin.MacroBean o1, org.javlo.module.admin.MacroBean o2) {
				return o1.getPriority() - o2.getPriority();
			}
		});
	}
	

}
