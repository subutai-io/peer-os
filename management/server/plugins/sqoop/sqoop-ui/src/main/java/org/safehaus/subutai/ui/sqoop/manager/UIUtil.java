package org.safehaus.subutai.ui.sqoop.manager;

import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

public class UIUtil {

	public static Button getButton(String caption, float width) {
		return getButton(caption, width, null);
	}

	public static Button getButton(String caption, float width, Button.ClickListener listener) {
		Button button = new Button(caption);
		button.addStyleName("default");
		button.setWidth(width, Sizeable.Unit.PIXELS);
		if (listener != null) button.addClickListener(listener);
		return button;
	}

	public static TextArea getTextArea(String caption, float width, float height) {
		TextArea textArea = new TextArea(caption);
		textArea.setWidth(width, Sizeable.Unit.PIXELS);
		textArea.setHeight(height, Sizeable.Unit.PIXELS);
		textArea.setWordwrap(false);
		return textArea;
	}

	public static Label getLabel(String text, float width) {
		return getLabel(text, width, Sizeable.Unit.PIXELS);
	}

	public static Label getLabel(String text, float width, Sizeable.Unit sizeAble) {
		Label label = new Label(text);
		label.setWidth(width, sizeAble);
		label.setContentMode(ContentMode.HTML);
		return label;
	}

	public static AbstractTextField getTextField(String label, float width) {
		return getTextField(label, width, false);
	}

	public static AbstractTextField getTextField(String label, float width, boolean isPassword) {
		AbstractTextField textField = isPassword
				? new PasswordField(label) : new TextField(label);
		textField.setWidth(width, Sizeable.Unit.PIXELS);
		return textField;
	}
}
