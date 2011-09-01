package common.typeutils;

import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;


public class ButtonFactory extends ResourceManager {
	// Constants
	//
	private static final String TYPE_BUTTON = "BUTTON";

	private static final String TYPE_BUTTON_EX = "BUTTON_EX";

	private static final String TYPE_BUTTON_OPT = "BUTTON_OPT";

	private static final String TYPE_TOGGLE_BUTTON = "TOGGLE_BUTTON";

	private static final String TYPE_TOGGLE_BUTTON_EX = "TOGGLE_BUTTON_EX";

	private static final String TYPE_TOGGLE_BUTTON_OPT = "TOGGLE_BUTTON_OPT";

	private static final String TYPE_CHECK_BOX = "CHECK_BOX";

	private static final String TYPE_RADIO_BUTTON = "RADIO_BUTTON";

	private static final String TYPE_SUFFIX = ".type";

	private static final String ICON_SUFFIX = ".icon";

	private static final String SMALL_ICON_SUFFIX = ".smallIcon";

	private static final String LARGE_ICON_SUFFIX = ".largeIcon";

	private static final String TEXT_SUFFIX = ".text";

	private static final String MNEMONIC_SUFFIX = ".mnemonic";

	private static final String COMMAND_SUFFIX = ".command";

	private static final String ACTION_SUFFIX = ".action";

	private static final String SELECTED_SUFFIX = ".selected";

	private static final String TOOLTIP_SUFFIX = ".tooltip";

	private static final String GROUP_SUFFIX = ".group";

	private static final String POPUP_SUFFIX = ".popup";

	private ActionMap actionMap;

	private MenuFactory menuFactory;

	private Map<String, ButtonGroup> groupMap = new HashMap<String, ButtonGroup>();

	public ButtonFactory(String baseName, ActionMap actionMap) {
		super(baseName);
		this.actionMap = actionMap;
	}

	public ButtonFactory(ResourceBundle bundle, ActionMap actionMap) {
		super(bundle);
		this.actionMap = actionMap;
	}

	public AbstractButton createAbstractButton(String name) {
		AbstractButton btn = null;
		String type;
		if (!contains(name + TYPE_SUFFIX)
				|| (type = getString(name + TYPE_SUFFIX)).equals(TYPE_BUTTON)) {
			btn = initializeButton(new JButton(), name);
		} else if (type.equals(TYPE_BUTTON_EX)) {
			btn = initializeButtonEx(new JButtonEx(), name);
		} else if (type.equals(TYPE_BUTTON_OPT)) {
			btn = initializeButtonOpt(new JButtonOpt(), name);
		} else if (type.equals(TYPE_TOGGLE_BUTTON)) {
			btn = initializeButton(new JToggleButton(), name);
		} else if (type.equals(TYPE_TOGGLE_BUTTON_EX)) {
			btn = initializeButtonEx(new JToggleButtonEx(), name);
		} else if (type.equals(TYPE_TOGGLE_BUTTON_OPT)) {
			btn = initializeButtonOpt(new JToggleButtonOpt(), name);
		} else if (type.equals(TYPE_CHECK_BOX)) {
			btn = initializeButtonEx(new JCheckBox(), name);
		} else if (type.equals(TYPE_RADIO_BUTTON)) {
			btn = initializeButtonEx(new JRadioButton(), name);
		}
		checkAndStore(name, btn);
		return btn;
	}

	public JButton createButton(String name) {
		return (JButton) createAbstractButton(name);
	}

	public JToggleButton createToggleButton(String name) {
		return (JToggleButton) createAbstractButton(name);
	}

	public JCheckBox createCheckBox(String name) {
		return (JCheckBox) createAbstractButton(name);
	}

	private AbstractButton initializeButton(AbstractButton ab, String name) {
		ab.setMargin(new Insets(3, 3, 3, 3));
		if (contains(name + TEXT_SUFFIX)) {
			ab.setText(getString(name + TEXT_SUFFIX));
		}
		Action action;
		if (contains(name + ACTION_SUFFIX)) {
			action = actionMap.getAction(getString(name + ACTION_SUFFIX));
			ABPCListener abpcl = new ABPCListener(ab);
			Boolean b;
			b = (Boolean) action.getValue(ABPCListener.PROPERTY_ENABLED);
			if (b != null) {
				ab.setEnabled(b);
			}
			b = (Boolean) action.getValue(ABPCListener.PROPERTY_SELECTED);
			if (b != null) {
				ab.setSelected(b);
			}
			action.addPropertyChangeListener(abpcl);
			ab.addActionListener(action);
		}
		if (contains(name + ICON_SUFFIX)) {
			ab.setIcon(getIcon(actionMap, name + ICON_SUFFIX));
			ab.setHorizontalTextPosition(SwingConstants.CENTER);
			ab.setVerticalTextPosition(SwingConstants.BOTTOM);
		}
		if (contains(name + MNEMONIC_SUFFIX)) {
			ab.setMnemonic(getChar(name + MNEMONIC_SUFFIX));
		}
		if (contains(name + COMMAND_SUFFIX)) {
			ab.setActionCommand(getString(name + COMMAND_SUFFIX));
		}
		if (contains(name + TOOLTIP_SUFFIX)) {
			ab.setToolTipText(getString(name + TOOLTIP_SUFFIX));
		}
		if (contains(name + GROUP_SUFFIX)) {
			String gn = getString(name + GROUP_SUFFIX);
			ButtonGroup bg = groupMap.get(gn);
			if (bg == null) {
				bg = new ButtonGroup();
				groupMap.put(gn, bg);
			}
			bg.add(ab);
		}
		if (contains(name + SELECTED_SUFFIX)) {
			ab.setSelected(getBool(name + SELECTED_SUFFIX));
		}
		return ab;
	}

	private AbstractButton initializeButtonEx(AbstractButton b, String name) {
		initializeButton(b, name);
		// Of course, a better way would be to create an abstract super class
		// AbstractButtonEx which is implemented by JButtonEx and
		// JToggleButtonEx
		// but this will have to do for now :(
		if (b instanceof JButtonEx) {
			JButtonEx jbx = (JButtonEx) b;
			Icon icon = jbx.getIcon();
			// Small Icon
			if (contains(name + SMALL_ICON_SUFFIX)) {
				jbx.setSmallIcon(getIcon(actionMap, name + SMALL_ICON_SUFFIX));
			} else {
				jbx.setSmallIcon(icon);
			}
			// Large Icon
			if (contains(name + LARGE_ICON_SUFFIX)) {
				jbx.setLargeIcon(getIcon(actionMap, name + LARGE_ICON_SUFFIX));
			} else {
				jbx.setLargeIcon(icon);
			}
		} else if (b instanceof JToggleButtonEx) {
			JToggleButtonEx jtbx = (JToggleButtonEx) b;
			Icon icon = jtbx.getIcon();
			// Small Icon
			if (contains(name + SMALL_ICON_SUFFIX)) {
				jtbx.setSmallIcon(getIcon(actionMap, name + SMALL_ICON_SUFFIX));
			} else {
				jtbx.setSmallIcon(icon);
			}
			// Large Icon
			if (contains(name + LARGE_ICON_SUFFIX)) {
				jtbx.setLargeIcon(getIcon(actionMap, name + LARGE_ICON_SUFFIX));
			} else {
				jtbx.setLargeIcon(icon);
			}
		}
		return b;
	}

	private AbstractButton initializeButtonOpt(AbstractButton b, String name) {
		initializeButtonEx(b, name);
		Insets m = b.getMargin();
		m.right += 15;
		b.setMargin(m);
		JButtonOpt jbo = (JButtonOpt) b;
		if (contains(name + POPUP_SUFFIX)) {
			if (menuFactory == null) {
				menuFactory = new MenuFactory(bundle, actionMap);
			}
			name = getString(name + POPUP_SUFFIX);
			JMenu menu = menuFactory.createMenu(name);
			JPopupMenu popup = menu.getPopupMenu();
			jbo.setPopupMenu(popup);
			checkAndStore(name, popup);
		}
		return b;
	}
}
