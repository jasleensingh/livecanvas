package common.typeutils;

import javax.swing.Icon;
import javax.swing.JButton;

public class JButtonEx extends JButton {

	public static final int TEXT_WITH_ICONS = 0, TEXT_ONLY = 1, ICONS_ONLY = 2;

	public static final int SMALL_ICONS = 0, LARGE_ICONS = 1;

	private int displayType;

	private int iconSize = SMALL_ICONS;

	private Icon icon, smallIcon, largeIcon;

	private String text;

	public JButtonEx() {
		this(null, ICONS_ONLY);
	}

	public JButtonEx(int displayType) {
		this(null, displayType);
	}

	public JButtonEx(String text, int displayType) {
		this(text, null, null, displayType);
	}

	public JButtonEx(Icon smallIcon, Icon largeIcon, int displayType) {
		this(null, smallIcon, largeIcon, displayType);
	}

	public JButtonEx(String text, Icon smallIcon, Icon largeIcon,
			int displayType) {
		this.text = text;
		this.smallIcon = smallIcon;
		this.largeIcon = largeIcon;
		setDisplayType(displayType);
		updateDisplay();
	}

	private void updateDisplay() {
		super.setIcon(getIcon());
		super.setText(getText());
	}

	private Icon getSizedIcon() {
		switch (getIconSize()) {
		case LARGE_ICONS:
			return getLargeIcon();
		case SMALL_ICONS:
			return getSmallIcon();
		}
		return null;
	}

	public int getDisplayType() {
		return displayType;
	}

	public void setDisplayType(int displayType) {
		switch (displayType) {
		case TEXT_ONLY:
		case ICONS_ONLY:
		case TEXT_WITH_ICONS:
			this.displayType = displayType;
			break;
		}
		updateDisplay();
	}

	public Icon getIcon() {
		switch (displayType) {
		case TEXT_ONLY:
			return null;
		case TEXT_WITH_ICONS:
		case ICONS_ONLY:
			return getSizedIcon();
		}
		return null;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
		updateDisplay();
	}

	public String getText() {
		switch (displayType) {
		case TEXT_WITH_ICONS:
		case TEXT_ONLY:
			return text;
		case ICONS_ONLY:
			return null;
		}
		return null;
	}

	public void setText(String text) {
		this.text = text;
		updateDisplay();
	}

	public Icon getLargeIcon() {
		return largeIcon != null ? largeIcon : icon;
	}

	public void setLargeIcon(Icon largeIcon) {
		this.largeIcon = largeIcon;
		updateDisplay();
	}

	public Icon getSmallIcon() {
		return smallIcon != null ? smallIcon : icon;
	}

	public void setSmallIcon(Icon smallIcon) {
		this.smallIcon = smallIcon;
		updateDisplay();
	}

	public int getIconSize() {
		return iconSize;
	}

	public void setIconSize(int iconSize) {
		this.iconSize = iconSize;
		updateDisplay();
	}
}
