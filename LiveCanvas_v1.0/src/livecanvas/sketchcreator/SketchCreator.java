package livecanvas.sketchcreator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import livecanvas.CanvasContainer;
import livecanvas.CanvasDraw;
import livecanvas.Perspective;
import livecanvas.Settings;
import livecanvas.Settings.SettingsContainer;
import livecanvas.Tool;
import livecanvas.Tool.Pointer.PointerHandler;
import livecanvas.Utils;
import livecanvas.Utils.ButtonType;

import common.typeutils.AutoPanel;
import common.typeutils.IntegerType;
import common.typeutils.PropertyFactory;

public class SketchCreator extends Perspective implements ColorBar.Listener,
		Tool.ToolContext {
	private static final Class clazz = SketchCreator.class;

	private JToolBar toolBar;
	private JMenuBar menuBar;
	private SketchCreatorSettings settings;
	private ColorBar colorBar;
	private Color selectedColor;
	private JToolBar tools;
	private CanvasDraw canvas;
	private CanvasContainer canvasContainer;
	private JPanel propertiesPanel;

	public SketchCreator() {
		super("Sketch Creator", new ImageIcon(
				clazz.getResource("res/sketch_creator.png")),
				new BorderLayout());
		setPreferredSize(new Dimension(1024, 600));
		setBackground(Color.darkGray);

		tools = createTools();
		add(tools, BorderLayout.WEST);

		settings = new SketchCreatorSettings();

		JPanel east = new JPanel(new BorderLayout());
		colorBar = new ColorBar();
		colorBar.setListener(this);
		east.add(colorBar, BorderLayout.NORTH);
		selectedColor = colorBar.getSelectedColor();
		propertiesPanel = new JPanel(new BorderLayout());
		east.add(propertiesPanel);
		add(east, BorderLayout.EAST);

		JPanel center = new JPanel(new BorderLayout());
		center.setBackground(Color.lightGray);
		canvas = new CanvasDraw(800, 600, PointerHandler.NULL);
		canvasContainer = new CanvasContainer(canvas);
		center.add(canvasContainer);

		add(center);

		setSelectedToolType(TOOLS_BRUSH);
	}

	@Override
	public boolean handleEvent(ActionEvent e) {
		switch (Integer.parseInt(e.getActionCommand())) {
		case EXIT:
			exit();
			break;
		case NEW:
			clear();
			break;
		case OPEN:
			open(null);
			break;
		case SAVE:
			save(null);
			break;
		case BGREF_SET:
			setBgRef();
			break;
		case BGREF_REMOVE:
			removeBgRef();
			break;
		case SETTINGS:
			showSettingsDialog();
			break;
		case TOOLS_PENCIL:
			setSelectedToolType(TOOLS_PENCIL);
			break;
		case TOOLS_BRUSH:
			setSelectedToolType(TOOLS_BRUSH);
			break;
		case TOOLS_PEN:
			setSelectedToolType(TOOLS_PEN);
			break;
		case TOOLS_ERASE:
			setSelectedToolType(TOOLS_ERASE);
			break;
		case TOOLS_SELECT:
			setSelectedToolType(TOOLS_SELECT);
			break;
		case TOOLS_POINTER:
			setSelectedToolType(TOOLS_POINTER);
			break;
		case TOOLS_PANZOOM:
			setSelectedToolType(TOOLS_PANZOOM);
			break;
		default:
			return false;
		}
		repaint();
		return true;
	}

	public JToolBar getToolBar() {
		if (toolBar == null) {
			toolBar = createToolBar();
		}
		return toolBar;
	}

	public JMenuBar getMenuBar() {
		if (menuBar == null) {
			menuBar = createMenuBar();
		}
		return menuBar;
	}

	public SketchCreatorSettings getSettings() {
		return settings;
	}

	@Override
	public void selectedColorChanged(Color newColor) {
		selectedColor = newColor;
		canvas.getSelectedTool().onSelected(this);
	}

	public Color getSelectedColor() {
		return selectedColor;
	}

	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		JButton button;
		toolbar.add(Utils.createToolBarButton("New sketch",
				new ImageIcon(clazz.getResource("res/new.png")),
				ButtonType.ICON_ONLY, NEW, "New", this));
		toolbar.add(Utils.createToolBarButton("Open",
				new ImageIcon(clazz.getResource("res/open.png")),
				ButtonType.ICON_ONLY, OPEN, "Open a saved sketch", this));
		toolbar.add(Utils.createToolBarButton("Save",
				new ImageIcon(clazz.getResource("res/save.png")),
				ButtonType.ICON_ONLY, SAVE, "Save mesh", this));
		toolbar.addSeparator();
		toolbar.add(Utils.createToolBarButton("Cut",
				new ImageIcon(clazz.getResource("res/cut.png")),
				ButtonType.ICON_ONLY, CUT, "Cut selection to clipboard", this));
		toolbar.add(Utils.createToolBarButton("Copy",
				new ImageIcon(clazz.getResource("res/copy.png")),
				ButtonType.ICON_ONLY, COPY, "Copy selection to clipboard", this));
		toolbar.add(Utils.createToolBarButton("Paste",
				new ImageIcon(clazz.getResource("res/paste.png")),
				ButtonType.ICON_ONLY, PASTE, "Paste content from clipboard",
				this));
		toolbar.addSeparator();
		toolbar.add(Utils.createToolBarButton("Undo",
				new ImageIcon(clazz.getResource("res/undo.png")),
				ButtonType.ICON_ONLY, UNDO, "Undo last action", this));
		toolbar.add(Utils.createToolBarButton("Redo",
				new ImageIcon(clazz.getResource("res/redo.png")),
				ButtonType.ICON_ONLY, REDO, "Redo last undone action", this));
		toolbar.addSeparator();
		toolbar.add(button = Utils.createToolBarButton("Set Ref.",
				new ImageIcon(clazz.getResource("res/set_background.png")),
				ButtonType.ICON_ONLY, BGREF_SET, "Set background reference...",
				this));
		toolbar.add(button = Utils.createToolBarButton("Remove", new ImageIcon(
				clazz.getResource("res/remove.png")), ButtonType.ICON_ONLY,
				BGREF_REMOVE, "Remove background reference...", this));
		return toolbar;
	}

	private JToolBar createTools() {
		JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
		JButton button;
		ButtonGroup toolsBg = new ButtonGroup();
		JToggleButton toolsButton;
		toolbar.add(toolsButton = Utils.createToolBarToggleButton("Pencil",
				new ImageIcon(clazz.getResource("res/pencil.png")),
				ButtonType.ICON_ONLY, TOOLS_PENCIL, "Pencil", toolsBg, this));
		toolbar.add(toolsButton = Utils.createToolBarToggleButton("Brush",
				new ImageIcon(clazz.getResource("res/brush.png")),
				ButtonType.ICON_ONLY, TOOLS_BRUSH, "Brush", toolsBg, this));
		toolsButton.setSelected(true);
		toolbar.add(toolsButton = Utils.createToolBarToggleButton("Pen",
				new ImageIcon(clazz.getResource("res/pen.png")),
				ButtonType.ICON_ONLY, TOOLS_PEN, "Pen", toolsBg, this));
		toolbar.add(toolsButton = Utils.createToolBarToggleButton("Erase",
				new ImageIcon(clazz.getResource("res/erase.png")),
				ButtonType.ICON_ONLY, TOOLS_ERASE, "Erase", toolsBg, this));
		toolbar.add(toolsButton = Utils.createToolBarToggleButton("Fill",
				new ImageIcon(clazz.getResource("res/fill.png")),
				ButtonType.ICON_ONLY, TOOLS_FILL, "Fill", toolsBg, this));
		toolbar.add(toolsButton = Utils.createToolBarToggleButton("Select",
				new ImageIcon(clazz.getResource("res/select.png")),
				ButtonType.ICON_ONLY, TOOLS_SELECT, "Select", toolsBg, this));
		toolbar.add(toolsButton = Utils.createToolBarToggleButton("Pointer",
				new ImageIcon(clazz.getResource("res/pointer.png")),
				ButtonType.ICON_ONLY, TOOLS_POINTER, "Pointer", toolsBg, this));
		toolbar.add(toolsButton = Utils.createToolBarToggleButton("PanZoom",
				new ImageIcon(clazz.getResource("res/panzoom.png")),
				ButtonType.ICON_ONLY, TOOLS_PANZOOM, "Pan or Zoom Canvas",
				toolsBg, this));
		return toolbar;
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu;
		JMenuItem menuItem;
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.add(menuItem = Utils.createMenuItem("New", NEW, KeyEvent.VK_N,
				"ctrl N", this));
		menu.add(menuItem = Utils.createMenuItem("Open...", OPEN,
				KeyEvent.VK_O, "ctrl O", this));
		menu.add(menuItem = Utils.createMenuItem("Save", SAVE, KeyEvent.VK_S,
				"ctrl S", this));
		menu.addSeparator();
		menu.add(menuItem = Utils.createMenuItem("Set Background Ref...",
				BGREF_SET, KeyEvent.VK_B, "", this));
		menu.add(menuItem = Utils.createMenuItem("Remove", BGREF_REMOVE,
				KeyEvent.VK_R, "", this));
		menu.addSeparator();
		menu.add(menuItem = Utils.createMenuItem("Exit", EXIT, KeyEvent.VK_X,
				"", this));
		menuBar.add(menu);
		menu = new JMenu("Edit");
		menu.setMnemonic(KeyEvent.VK_E);
		menu.add(menuItem = Utils.createMenuItem("Undo", UNDO, KeyEvent.VK_U,
				"ctrl Z", this));
		menu.add(menuItem = Utils.createMenuItem("Redo", REDO, KeyEvent.VK_R,
				"ctrl Y", this));
		menu.addSeparator();
		menu.add(menuItem = Utils.createMenuItem("Cut", CUT, KeyEvent.VK_T,
				"ctrl X", this));
		menu.add(menuItem = Utils.createMenuItem("Copy", COPY, KeyEvent.VK_C,
				"ctrl C", this));
		menu.add(menuItem = Utils.createMenuItem("Paste", PASTE, KeyEvent.VK_P,
				"ctrl V", this));
		menu.addSeparator();
		menu.add(menuItem = Utils.createMenuItem("Select All", SELECT_ALL,
				KeyEvent.VK_A, "ctrl A", this));
		menu.add(menuItem = Utils.createMenuItem("Invert Selection",
				INVERT_SELECTION, 0, "", this));
		menu.addSeparator();
		menu.add(menuItem = Utils.createMenuItem("Settings...", SETTINGS,
				KeyEvent.VK_S, "", this));
		menuBar.add(menu);
		menu = new JMenu("Tools");
		menu.setMnemonic(KeyEvent.VK_T);
		menu.add(menuItem = Utils.createMenuItem("Pencil", TOOLS_PENCIL,
				KeyEvent.VK_C, "C", this));
		menu.add(menuItem = Utils.createMenuItem("Brush", TOOLS_BRUSH,
				KeyEvent.VK_B, "B", this));
		menu.add(menuItem = Utils.createMenuItem("Pen", TOOLS_PEN,
				KeyEvent.VK_N, "N", this));
		menu.add(menuItem = Utils.createMenuItem("Erase", TOOLS_ERASE,
				KeyEvent.VK_E, "E", this));
		menu.add(menuItem = Utils.createMenuItem("Fill", TOOLS_FILL,
				KeyEvent.VK_F, "F", this));
		menu.add(menuItem = Utils.createMenuItem("Select", TOOLS_SELECT,
				KeyEvent.VK_S, "S", this));
		menu.add(menuItem = Utils.createMenuItem("Pointer", TOOLS_POINTER,
				KeyEvent.VK_P, "P", this));
		menu.add(menuItem = Utils.createMenuItem("Pan / Zoom", TOOLS_PANZOOM,
				KeyEvent.VK_Z, "Z", this));
		menuBar.add(menu);
		return menuBar;
	}

	private void setSelectedToolType(int type) {
		Tool prev, next;
		prev = canvas.getSelectedTool();
		canvas.setSelectedToolType(type);
		next = canvas.getSelectedTool();
		JToggleButton toolBtn;
		if (!next.onSelected(this)) {
			toolBtn = findToolButtonByName(prev.name);
			toolBtn.setSelected(true);
			canvas.setSelectedTool(prev);
			return;
		} else if (prev != null) {
			prev.onDeselected(this);
		}
		if (!(toolBtn = findToolButtonByName(next.name)).isSelected()) {
			toolBtn.setSelected(true);
		}
		propertiesPanel.removeAll();
		propertiesPanel.add(new AutoPanel(PropertyFactory
				.createProperties(next)));
		propertiesPanel.revalidate();
		propertiesPanel.repaint();
	}

	private JToggleButton findToolButtonByName(String name) {
		List<JToggleButton> toolBtns = Utils.findComponentsOfType(tools,
				JToggleButton.class);
		for (JToggleButton toolBtn : toolBtns) {
			if (toolBtn.getName().equals(name)) {
				return toolBtn;
			}
		}
		return null;
	}

	private void clear() {
	}

	private void open(File file) {
		if (file == null) {
			FileDialog fd = new FileDialog(
					JOptionPane.getFrameForComponent(SketchCreator.this),
					"Open");
			fd.setVisible(true);
			String file_str = fd.getFile();
			if (file_str == null) {
				return;
			}
			file = new File(fd.getDirectory() + "/" + file_str);
		}
		try {
			BufferedImage img = ImageIO.read(file);
			canvas.getCurrCel().buf = img;
		} catch (Exception e1) {
			e1.printStackTrace();
			String msg = "An error occurred while trying to load.";
			JOptionPane.showMessageDialog(
					JOptionPane.getFrameForComponent(SketchCreator.this), msg,
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		repaint();
	}

	private boolean canSave() {
		return true;
	}

	private void save(File file) {
		if (!canSave()) {
			return;
		}
		if (file == null) {
			FileDialog fd = new FileDialog(
					JOptionPane.getFrameForComponent(SketchCreator.this),
					"Save");
			fd.setVisible(true);
			String file_str = fd.getFile();
			if (file_str == null) {
				return;
			}
			file = new File(fd.getDirectory() + "/" + file_str);
		}
		try {
			String name = file.getName();
			BufferedImage celBuf = canvas.getCurrCel().buf;
			BufferedImage destImage = new BufferedImage(celBuf.getWidth(),
					celBuf.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g = destImage.getGraphics();
			if (canvas.getSettings().canvasOpaque) {
				g.setColor(canvas.getSettings().canvasBackgroundColor);
				g.fillRect(0, 0, destImage.getWidth(), destImage.getHeight());
			}
			g.drawImage(celBuf, 0, 0, null);
			g.dispose();
			if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
				ImageIO.write(destImage, "jpg", file);
			} else if (name.endsWith(".png")) {
				ImageIO.write(destImage, "png", file);
			} else {
				name += ".png";
				file = new File(file.getParent() + "/" + name);
				ImageIO.write(destImage, "png", file);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			String msg = "An error occurred while trying to load.";
			JOptionPane.showMessageDialog(
					JOptionPane.getFrameForComponent(SketchCreator.this), msg,
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		repaint();
	}

	public void setBgRef() {
		String path = getImagePath(this);
		if (path == null) {
			return;
		}
		try {
			canvas.getCurrCel().backgroundRef = ImageIO.read(new File(path));
		} catch (IOException e) {
			String msg = "Could not load image!";
			JOptionPane.showMessageDialog(this, msg, "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		repaint();
	}

	public void removeBgRef() {
		canvas.getCurrCel().backgroundRef = null;
		repaint();
	}

	private String getImagePath(Component parent) {
		FileDialog fd = new FileDialog(
				JOptionPane.getFrameForComponent(parent), "Load Image");
		fd.setVisible(true);
		String file_str = fd.getFile();
		if (file_str == null) {
			return null;
		}
		return fd.getDirectory() + "/" + file_str;
	}

	private void exit() {
		if (JOptionPane.showConfirmDialog(SketchCreator.this,
				"Are you sure you want to exit?", "Exit",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		System.exit(0);
	}

	public CanvasDraw getCanvas() {
		return canvas;
	}

	public CanvasContainer getCanvasContainer() {
		return canvasContainer;
	}

	public static class SketchCreatorSettings extends SettingsContainer {
		public static final String GENERAL = "General";

		@IntegerType(name = "Undo Steps", description = "Select the number of Undo steps to preserve while editing", min = 0, max = 50, category = GENERAL)
		public int undoSteps = 10;

		public SketchCreatorSettings(Settings... settings) {
			super(settings);
		}

		@Override
		public Settings clone() {
			SketchCreatorSettings clone = new SketchCreatorSettings(
					containedClone());
			clone.undoSteps = undoSteps;
			return clone;
		}

		@Override
		public void copyFrom(Settings copy) {
			SketchCreatorSettings s = (SketchCreatorSettings) copy;
			undoSteps = s.undoSteps;
			containedCopyFrom(s);
		}

		@Override
		public String[] getCategories() {
			List<String> list = new LinkedList<String>();
			list.add(GENERAL);
			list.addAll(Arrays.asList(containedCategories()));
			return list.toArray(new String[0]);
		}
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		JFrame f = new JFrame("LiveCanvas - Sketch Creator");
		final SketchCreator sketchCreator = new SketchCreator();
		f.setJMenuBar(sketchCreator.getMenuBar());
		f.getContentPane().add(sketchCreator);
		f.getContentPane().add(sketchCreator.getToolBar(), BorderLayout.NORTH);
		f.pack();
		f.setLocationRelativeTo(null);
		f.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		f.setVisible(true);
	}
}
