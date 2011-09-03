package livecanvas;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;

import common.typeutils.AutoPanel;
import common.typeutils.Property;
import common.typeutils.PropertyContainer;
import common.typeutils.PropertyFactory;

public abstract class Settings {
	public static interface Listener {
		public void settingsChanged(Settings settings);
	}

	private Listener listener;

	public Listener getListener() {
		return listener;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	protected void notifyListener() {
		if (listener == null) {
			return;
		}
		listener.settingsChanged(this);
	}

	public abstract Settings clone();

	public abstract void copyFrom(Settings copy);

	public abstract String[] getCategories();

	public static void showDialog(Component parent, final Settings settings) {
		final JDialog d = new JDialog(JOptionPane.getFrameForComponent(parent),
				"Settings", true);
		JPanel center = new JPanel(new BorderLayout());
		center.setPreferredSize(new Dimension(500, 400));
		final Settings tempCopy = settings.clone();
		String[] cats = settings.getCategories();
		JPanel propertiesPanel;
		Property[] properties = PropertyFactory.createProperties(tempCopy);
		if (cats == null) {
			propertiesPanel = new AutoPanel(properties);
		} else {
			propertiesPanel = new JPanel(new BorderLayout());
			propertiesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,
					5));
			JTabbedPane tabs = new JTabbedPane();
			for (String cat : cats) {
				tabs.addTab(cat, new AutoPanel(properties, cat));
			}
			propertiesPanel.add(tabs);
		}
		center.add(propertiesPanel);
		d.getContentPane().add(center);
		JPanel south = new JPanel(new BorderLayout());
		south.add(new JSeparator(), BorderLayout.NORTH);
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		JButton apply = new JButton("Apply");
		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				settings.copyFrom(tempCopy);
				settings.notifyListener();
				d.setVisible(false);
			}
		});
		buttons.add(apply);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				d.setVisible(false);
			}
		});
		buttons.add(cancel);
		south.add(buttons);
		d.getContentPane().add(south, BorderLayout.SOUTH);
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.pack();
		d.setLocationRelativeTo(d.getParent());
		d.setVisible(true);
	}

	public abstract static class SettingsContainer extends Settings implements
			PropertyContainer {
		protected List<Settings> contained = new LinkedList<Settings>();

		public SettingsContainer(Settings... settings) {
			contained.addAll(Arrays.asList(settings));
		}

		@Override
		protected void notifyListener() {
			for (Settings s : contained) {
				s.notifyListener();
			}
		}

		@Override
		public Property[] getContainedProperties() {
			List<Property> props = new ArrayList<Property>();
			for (Settings s : contained) {
				props.addAll(Arrays.asList(PropertyFactory.createProperties(s)));
			}
			return props.toArray(new Property[0]);
		}

		protected Settings[] containedClone() {
			List<Settings> containedClone = new LinkedList<Settings>();
			for (Settings s : contained) {
				containedClone.add(s.clone());
			}
			return containedClone.toArray(new Settings[0]);
		}

		protected void containedCopyFrom(SettingsContainer copy) {
			int i = 0;
			for (Settings s : copy.contained) {
				contained.get(i++).copyFrom(s);
			}
		}

		public String[] containedCategories() {
			List<String> containedCats = new LinkedList<String>();
			for (Settings s : contained) {
				containedCats.addAll(Arrays.asList(s.getCategories()));
			}
			return containedCats.toArray(new String[0]);
		}
	}
}
