package org.javlo.macro;

import org.javlo.macro.core.IInteractiveMacro;

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
