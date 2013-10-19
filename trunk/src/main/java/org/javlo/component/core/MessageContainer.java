package org.javlo.component.core;

public class MessageContainer extends AbstractVisualComponent {
	
	public static final String TYPE = "message-container";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getHexColor() {
		return IContentVisualComponent.CONTAINER_COLOR; 
	}
	
	@Override
	protected boolean isNeedRenderer() {
		return true;
	}

}
