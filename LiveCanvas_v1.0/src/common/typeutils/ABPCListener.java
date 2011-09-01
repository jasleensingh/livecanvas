package common.typeutils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;

public class ABPCListener implements PropertyChangeListener {
	public static final String PROPERTY_ENABLED = "enabled";

	public static final String PROPERTY_SELECTED = "selected";

	public static final String PROPERTY_VISIBLE = "visible";

	private AbstractButton ab;

	public ABPCListener(AbstractButton ab) {
		this.ab = ab;
	}

	public void propertyChange(PropertyChangeEvent e) {
		String name = e.getPropertyName();
		if (name.equals(PROPERTY_ENABLED)) {
			ab.setEnabled((Boolean) e.getNewValue());
		} else if (name.equals(PROPERTY_SELECTED)) {
			ab.setSelected((Boolean) e.getNewValue());
		} else if (name.equals(PROPERTY_VISIBLE)) {
			ab.setVisible((Boolean) e.getNewValue());
		}
	}
}
