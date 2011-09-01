package common.typeutils;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;


public class MenuFactory extends ResourceManager {
	// Constants
	//
	private static final String TYPE_MENU = "MENU";

	private static final String TYPE_ITEM = "ITEM";

	private static final String TYPE_RADIO = "RADIO";

	private static final String TYPE_CHECK = "CHECK";

	private static final String ICON_BLANK = "BLANK";

	private static final String SEPARATOR = "-";

	private static final String TYPE_SUFFIX = ".type";

	private static final String TEXT_SUFFIX = ".text";

	private static final String MNEMONIC_SUFFIX = ".mnemonic";

	private static final String COMMAND_SUFFIX = ".command";

	private static final String ACCELERATOR_SUFFIX = ".accelerator";

	private static final String ACTION_SUFFIX = ".action";

	private static final String SELECTED_SUFFIX = ".selected";

	private static final String ENABLED_SUFFIX = ".enabled";

	private static final String VISIBLE_SUFFIX = ".visible";

	private static final String ICON_SUFFIX = ".icon";

	private static final String GROUP_SUFFIX = ".group";

	private ActionMap actionMap;

	private Map<String, ButtonGroup> groupMap = new HashMap<String, ButtonGroup>();

	public MenuFactory(String baseName, ActionMap actionMap) {
		super(baseName);
		this.actionMap = actionMap;
	}

	public MenuFactory(ResourceBundle bundle, ActionMap actionMap) {
		super(bundle);
		this.actionMap = actionMap;
	}

	public JMenuBar createMenuBar(String name) {
		JMenuBar menubar = new JMenuBar();
		checkAndStore(name, menubar);
		String[] components = getStringArray(name);
		for (String comp : components) {
			JMenu menu = (JMenu) createMenuComponent(comp);
			menubar.add(menu);
		}
		return menubar;
	}

	private HashMap<String, ButtonGroup> buttonGroups = new HashMap<String, ButtonGroup>();

	public JComponent createMenuComponent(String name) {
		String type = getString(name + TYPE_SUFFIX);
		JComponent comp = null;
		if (type.equals(TYPE_MENU)) {
			comp = createMenu(name);
		} else if (type.equals(TYPE_ITEM)) {
			comp = createMenuItem(name);
		} else if (type.equals(TYPE_RADIO)) {
			comp = createRadioButtonMenuItem(name);
		} else if (type.equals(TYPE_CHECK)) {
			comp = createCheckBoxMenuItem(name);
		}
		checkAndStore(name, comp);
		return comp;
	}

	public JMenu createMenu(String name) {
		JMenu menu = new JMenu();
		initializeMenuItem(menu, name);
		String[] items = getStringArray(name);
		for (String item : items) {
			if (item.length() <= 0) {
				continue;
			}
			if (item.equals(SEPARATOR)) {
				menu.addSeparator();
			} else {
				menu.add(createMenuComponent(item));
			}
		}
		return menu;
	}

	public JMenuItem createMenuItem(String name) {
		JMenuItem item = new JMenuItem();
		initializeMenuItem(item, name);
		return item;
	}

	public JMenuItem createRadioButtonMenuItem(String name) {
		JMenuItem item = new JRadioButtonMenuItem();
		initializeMenuItem(item, name);
		return item;
	}

	public JMenuItem createCheckBoxMenuItem(String name) {
		JMenuItem item = new JCheckBoxMenuItem();
		initializeMenuItem(item, name);
		return item;
	}

	private JMenuItem initializeMenuItem(final JMenuItem m, String name) {
		if (contains(name + TEXT_SUFFIX)) {
			m.setText(getString(name + TEXT_SUFFIX));
		}
		Action action = null;
		if (contains(name + ACTION_SUFFIX)) {
			action = actionMap.getAction(getString(name + ACTION_SUFFIX));
			ABPCListener abpcl = new ABPCListener(m);
			action.addPropertyChangeListener(abpcl);
			Boolean b;
			b = (Boolean) action.getValue(ABPCListener.PROPERTY_ENABLED);
			if (b != null) {
				m.setEnabled(b);
			}
			b = (Boolean) action.getValue(ABPCListener.PROPERTY_SELECTED);
			if (b != null) {
				m.setSelected(b);
			}
			m.addActionListener(action);
		}
		if (contains(name + ICON_SUFFIX)) {
			m.setIcon(getIcon(actionMap, name + ICON_SUFFIX));
		}
		if (contains(name + MNEMONIC_SUFFIX)) {
			m.setMnemonic(getChar(name + MNEMONIC_SUFFIX));
		}
		if (contains(name + COMMAND_SUFFIX)) {
			m.setActionCommand(getString(name + COMMAND_SUFFIX));
		}
		if (contains(name + ACCELERATOR_SUFFIX)) {
			m.setAccelerator(KeyStroke.getKeyStroke(getString(name
					+ ACCELERATOR_SUFFIX)));
		}
		if (contains(name + SELECTED_SUFFIX)) {
			boolean b = getBool(name + SELECTED_SUFFIX);
			m.setSelected(b);
			if (action != null) {
				action.putValue(ABPCListener.PROPERTY_SELECTED, b);
			}
		}
		if (contains(name + ENABLED_SUFFIX)) {
			boolean b = getBool(name + ENABLED_SUFFIX);
			m.setEnabled(b);
			if (action != null) {
				action.putValue(ABPCListener.PROPERTY_ENABLED, b);
			}
		}
		if (contains(name + VISIBLE_SUFFIX)) {
			boolean b = getBool(name + VISIBLE_SUFFIX);
			m.setVisible(b);
			if (action != null) {
				action.putValue(ABPCListener.PROPERTY_VISIBLE, b);
			}
		}
		if (contains(name + GROUP_SUFFIX)) {
			String gn = getString(name + GROUP_SUFFIX);
			ButtonGroup bg = groupMap.get(gn);
			if (bg == null) {
				bg = new ButtonGroup();
				groupMap.put(gn, bg);
			}
			bg.add(m);
		}
		return m;
	}
}
