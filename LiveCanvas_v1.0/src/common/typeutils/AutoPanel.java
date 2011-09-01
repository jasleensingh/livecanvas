package common.typeutils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import common.typeutils.MyWidgets.MyCheckBox;
import common.typeutils.MyWidgets.MyColorField;
import common.typeutils.MyWidgets.MyComboBox;
import common.typeutils.MyWidgets.MyDateField;
import common.typeutils.MyWidgets.MyDirectoryField;
import common.typeutils.MyWidgets.MyFileField;
import common.typeutils.MyWidgets.MyFontField;
import common.typeutils.MyWidgets.MyGradientField;
import common.typeutils.MyWidgets.MyListPanel;
import common.typeutils.MyWidgets.MyPasswordField;
import common.typeutils.MyWidgets.MyPrefixedTextField;
import common.typeutils.MyWidgets.MySpinner;
import common.typeutils.MyWidgets.MyStrokeField;
import common.typeutils.MyWidgets.MyTextArea;
import common.typeutils.MyWidgets.MyTextField;



public class AutoPanel extends JPanel {
	private static final Insets INSETS = new Insets(2, 2, 2, 2);

	protected JPanel mainPanel;

	public AutoPanel(Property[] props) {
		this(props, "");
	}

	public AutoPanel(Property[] props, String cat) {
		super(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setOpaque(false);
		add(mainPanel = new JPanel(new GridBagLayout()), BorderLayout.NORTH);
		mainPanel.setOpaque(false);
		for (Property prop : props) {
			try {
				if (cat == null || cat.length() <= 0
						|| cat.equals(prop.category)) {
					if (prop instanceof BooleanProperty) {
						BooleanProperty bp = (BooleanProperty) prop;
						JCheckBox chk = new MyCheckBox(bp);
						addField(bp.name, prop.description, chk);
					} else if (prop instanceof DoubleProperty) {
						DoubleProperty dp = (DoubleProperty) prop;
						JSpinner spn = new MySpinner(dp);
						addField(dp.name, dp.description, spn);
					} else if (prop instanceof FloatProperty) {
						FloatProperty fp = (FloatProperty) prop;
						JSpinner spn = new MySpinner(fp);
						addField(fp.name, fp.description, spn);
					} else if (prop instanceof EnumProperty) {
						EnumProperty ep = (EnumProperty) prop;
						JComboBox cbo = new MyComboBox(ep);
						addField(ep.name, ep.description, cbo);
					} else if (prop instanceof IntegerProperty) {
						IntegerProperty ip = (IntegerProperty) prop;
						JSpinner spn = new MySpinner(ip);
						addField(ip.name, ip.description, spn);
					} else if (prop instanceof StringProperty) {
						StringProperty sp = (StringProperty) prop;
						JTextField tf = new MyTextField(sp);
						addField(sp.name, sp.description, tf);
					} else if (prop instanceof PrefixedStringProperty) {
						PrefixedStringProperty psp = (PrefixedStringProperty) prop;
						JPanel pnl = new MyPrefixedTextField(psp);
						addField(psp.name, psp.description, pnl);
					} else if (prop instanceof PasswordProperty) {
						PasswordProperty pp = (PasswordProperty) prop;
						JPasswordField pf = new MyPasswordField(pp);
						addField(pp.name, pp.description, pf);
						// } else if (prop instanceof StringArrayProperty) {
						// StringArrayProperty sap = (StringArrayProperty) prop;
						// JPanel alf = new MyArrayList(sap);
						// addField(sap.name, alf);
					} else if (prop instanceof TextProperty) {
						TextProperty tp = (TextProperty) prop;
						JTextArea ta = new MyTextArea(tp);
						ta.setRows(5);
						JScrollPane scp = new JScrollPane(ta);
						addField(tp.name, tp.description, scp, ta.getRows());
					} else if (prop instanceof FontProperty) {
						FontProperty fp = (FontProperty) prop;
						JPanel pnl = new MyFontField(fp);
						if (cat.length() <= 0 || cat.equals(fp.category)) {
							addField(fp.name, fp.description, pnl);
						}
					} else if (prop instanceof ColorProperty) {
						ColorProperty cp = (ColorProperty) prop;
						JPanel pnl = new MyColorField(cp);
						addField(cp.name, cp.description, pnl);
					} else if (prop instanceof StrokeProperty) {
						StrokeProperty sp = (StrokeProperty) prop;
						JPanel pnl = new MyStrokeField(sp);
						addField(sp.name, sp.description, pnl);
					} else if (prop instanceof FileProperty) {
						FileProperty fp = (FileProperty) prop;
						JPanel pnl = new MyFileField(fp);
						addField(fp.name, fp.description, pnl);
					} else if (prop instanceof DirectoryProperty) {
						DirectoryProperty dp = (DirectoryProperty) prop;
						JPanel pnl = new MyDirectoryField(dp);
						addField(dp.name, dp.description, pnl);
					} else if (prop instanceof DateProperty) {
						DateProperty dp = (DateProperty) prop;
						JTextField tf = new MyDateField(dp);
						addField(dp.name, dp.description, tf);
					} else if (prop instanceof GradientProperty) {
						GradientProperty gp = (GradientProperty) prop;
						JPanel pnl = new MyGradientField(gp);
						addField(gp.name, gp.description, pnl);
					} else if (prop instanceof ListProperty) {
						ListProperty lp = (ListProperty) prop;
						MyListPanel pnl = new MyListPanel(lp);
						addField(lp.name, lp.description, pnl, 5);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Container getContentPane() {
		return mainPanel;
	}

	public void addField(String propName, String description, Component comp) {
		addField(propName, description, comp, 1);
	}

	private Map<String, Field> fieldsMap = new HashMap<String, Field>();

	public void addField(String propName, String description, Component comp,
			int rows) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = 0.25;
		gc.weighty = 1.0;
		gc.gridheight = rows;
		gc.insets = INSETS;
		Component display = getDisplayComponent(propName, description);
		mainPanel.add(display, gc);
		gc = new GridBagConstraints();
		gc.gridx = 1;
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = 0.75;
		gc.weighty = 1.0;
		gc.gridheight = rows;
		gc.insets = INSETS;
		mainPanel.add(comp, gc);
		fieldsMap.put(propName, new Field(display, comp));
	}

	public void removeField(String propName) {
		Field f = fieldsMap.get(propName);
		if (f != null) {
			mainPanel.remove(f.display);
			mainPanel.remove(f.input);
			fieldsMap.remove(propName);
		}
	}

	protected Component getDisplayComponent(String propName, String description) {
		JLabel label = new JLabel(propName);
		if (description != null && description.length() > 0) {
			label.setToolTipText(description);
		}
		label.setVerticalAlignment(SwingConstants.TOP);
		return label;
	}

	private static class Field {
		public final Component display;

		public final Component input;

		public Field(Component display, Component input) {
			this.display = display;
			this.input = input;
		}
	}
}
