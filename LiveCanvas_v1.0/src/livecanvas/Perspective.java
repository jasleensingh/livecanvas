package livecanvas;

import java.awt.LayoutManager;

import javax.swing.Icon;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

public abstract class Perspective extends View {
	public final String name;
	public final Icon icon;

	public Perspective(String name, Icon icon, LayoutManager layout) {
		super(layout);
		this.name = name;
		this.icon = icon;
	}

	public abstract JToolBar getToolBar();

	public abstract JMenuBar getMenuBar();

	public abstract Settings getSettings();

	public boolean onSelected(Perspective last) {
		return true;
	}

	public boolean onDeselected(Perspective next) {
		return true;
	}

	public void showSettingsDialog() {
		Settings.showDialog(this, getSettings());
	}
}
