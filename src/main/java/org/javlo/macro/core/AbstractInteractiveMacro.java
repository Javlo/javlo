package org.javlo.macro.core;

public abstract class AbstractInteractiveMacro implements IInteractiveMacro {

	public AbstractInteractiveMacro() {
	}
	
	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public boolean isPreview() {
		return false;
	}

}
