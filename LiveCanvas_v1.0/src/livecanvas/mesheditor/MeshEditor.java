package livecanvas.mesheditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import livecanvas.AffineTransformPointerHandler;
import livecanvas.CanvasContainer;
import livecanvas.CanvasMesh;
import livecanvas.Mesh;
import livecanvas.Path;
import livecanvas.Perspective;
import livecanvas.PointerHandler;
import livecanvas.RenderImageTask;
import livecanvas.Settings;
import livecanvas.Settings.SettingsContainer;
import livecanvas.Tool;
import livecanvas.Utils;
import livecanvas.Utils.ButtonType;
import livecanvas.components.Layer;
import livecanvas.components.LayersView;
import livecanvas.components.ViewpointsView;
import livecanvas.image.RenderData;
import livecanvas.image.Style;

import org.json.JSONObject;

import common.typeutils.AutoPanel;
import common.typeutils.BooleanType;
import common.typeutils.DirectoryType;
import common.typeutils.EnumType;
import common.typeutils.IntegerType;
import common.typeutils.PropertyFactory;
import common.typeutils.StringType;

public class MeshEditor extends Perspective implements Tool.ToolContext,
		LayersView.Listener, ViewpointsView.Listener {
	private static final Class clazz = MeshEditor.class;

	private JToolBar toolBar;
	private JMenuBar menuBar;
	private RenderImageSettings renderImageSettings;
	private MeshEditorSettings settings;
	private Color selectedColor;
	private JToolBar tools;
	private CanvasMesh canvas;
	private CanvasContainer canvasContainer;
	private JPanel propertiesPanel;
	private JComboBox rendererSelect;
	private LayersView layersView;
	private ViewpointsView viewpointsView;

	private PointerHandler pointerHandler;

	public MeshEditor() {
		super("Mesh Editor", new ImageIcon(
				clazz.getResource("res/mesh_editor.png")), new BorderLayout());
		setPreferredSize(new Dimension(1024, 600));
		setBackground(Color.darkGray);

		tools = createTools();
		add(tools, BorderLayout.WEST);

		selectedColor = Color.black;
		JPanel east = new JPanel(new BorderLayout());
		east.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		Layer rootLayer = new Layer("ROOT");
		rootLayer.addSubLayer(new Layer("Layer1"));
		layersView = new LayersView(rootLayer);
		chain(layersView);
		layersView.addListener(this);
		east.add(layersView, BorderLayout.NORTH);
		propertiesPanel = new JPanel(new BorderLayout());
		east.add(propertiesPanel);
		viewpointsView = new ViewpointsView(layersView.getSelectedLayer());
		viewpointsView.addListener(this);
		east.add(viewpointsView, BorderLayout.SOUTH);
		add(east, BorderLayout.EAST);

		JPanel center = new JPanel(new BorderLayout());
		center.setBackground(Color.lightGray);
		canvas = new CanvasMesh(800, 600);
		canvas.setCurrLayer(getRootLayer());
		pointerHandler = new AffineTransformPointerHandler(canvas);
		canvas.setPointerHandler(pointerHandler);
		canvasContainer = new CanvasContainer(canvas);
		rootLayer.setCanvas(canvas);
		center.add(canvasContainer);

		renderImageSettings = new RenderImageSettings();
		settings = new MeshEditorSettings(canvas.getSettings(),
				layersView.getSettings(), renderImageSettings);

		add(center);
		layersView.layersList.setSelectedIndex(1);
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
		case SETTINGS:
			showSettingsDialog();
			break;
		case SEE_THROUGH:
			toggleOnionSkin();
			break;
		case SHOW_MESH:
			toggleShowMesh();
			break;
		case RENDER_SETTINGS:
			renderSettings();
			break;
		case RENDER:
			showImage(render());
			break;
		case TOOLS_BRUSH:
			setSelectedToolType(TOOLS_BRUSH);
			break;
		case TOOLS_PEN:
			setSelectedToolType(TOOLS_PEN);
			break;
		case TOOLS_MAGICWAND:
			setSelectedToolType(TOOLS_MAGICWAND);
			break;
		case TOOLS_SETCONTROLPOINTS:
			setSelectedToolType(TOOLS_SETCONTROLPOINTS);
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

	public Layer getRootLayer() {
		return layersView.getRootLayer();
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

	public MeshEditorSettings getSettings() {
		return settings;
	}

	public Color getSelectedColor() {
		return selectedColor;
	}

	@Override
	public void layerSelectionChanged(Layer selectedLayer) {
		canvas.setCurrLayer(selectedLayer);
		viewpointsView.layerChanged(selectedLayer);
		repaint();
	}

	@Override
	public void viewpointChanged(int vx, int vy) {
		layersView.getSelectedLayer().setCurrViewpoint(vx, vy);
		repaint();
	}

	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		JButton button;
		toolbar.add(Utils.createToolBarButton("New mesh",
				new ImageIcon(clazz.getResource("res/new.png")),
				ButtonType.ICON_ONLY, NEW, "New", this));
		toolbar.add(Utils.createToolBarButton("Open",
				new ImageIcon(clazz.getResource("res/open.png")),
				ButtonType.ICON_ONLY, OPEN, "Open a saved mesh", this));
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
		JPanel rendererSelectContainer = new JPanel();
		rendererSelectContainer.setOpaque(false);
		rendererSelectContainer.setMaximumSize(new Dimension(150, 40));
		rendererSelectContainer
				.add(rendererSelect = new JComboBox(Style.Styles));
		rendererSelect.setSelectedIndex(Style.Styles.length - 1);
		toolbar.add(rendererSelectContainer);
		toolbar.add(Utils.createToolBarButton("Render Settings", new ImageIcon(
				clazz.getResource("res/render_settings.png")),
				ButtonType.ICON_ONLY, RENDER_SETTINGS, "Render Settings", this));
		toolbar.add(Utils.createToolBarButton("Render",
				new ImageIcon(clazz.getResource("res/render.png")),
				ButtonType.ICON_ONLY, RENDER,
				"Render mesh using current style", this));
		return toolbar;
	}

	private JToolBar createTools() {
		JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
		ButtonGroup toolsBg = new ButtonGroup();
		JToggleButton toolsButton;
		toolbar.add(toolsButton = Utils.createToolBarToggleButton("Brush",
				new ImageIcon(clazz.getResource("res/brush.png")),
				ButtonType.ICON_ONLY, TOOLS_BRUSH, "Brush", toolsBg, this));
		toolsButton.setSelected(true);
		toolbar.add(toolsButton = Utils.createToolBarToggleButton("Pen",
				new ImageIcon(clazz.getResource("res/pen.png")),
				ButtonType.ICON_ONLY, TOOLS_PEN, "Pen", toolsBg, this));
		toolbar.add(toolsButton = Utils.createToolBarToggleButton("MagicWand",
				new ImageIcon(clazz.getResource("res/wand.png")),
				ButtonType.ICON_ONLY, TOOLS_MAGICWAND, "Magic Wand", toolsBg,
				this));
		toolbar.add(toolsButton = Utils.createToolBarToggleButton(
				"SetControlPoints",
				new ImageIcon(clazz.getResource("res/set_controls.png")),
				ButtonType.ICON_ONLY, TOOLS_SETCONTROLPOINTS,
				"Set Control Points", toolsBg, this));
		toolbar.add(toolsButton = Utils.createToolBarToggleButton("Pointer",
				new ImageIcon(clazz.getResource("res/pointer.png")),
				ButtonType.ICON_ONLY, TOOLS_POINTER, "Pointer", toolsBg, this));
		toolbar.add(toolsButton = Utils.createToolBarToggleButton("PanZoom",
				new ImageIcon(clazz.getResource("res/panzoom.png")),
				ButtonType.ICON_ONLY, TOOLS_PANZOOM, "Pan or Zoom Canvas",
				toolsBg, this));
		// toolsButton.setSelected(true);
		return toolbar;
	}

	public JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu, subMenu;
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
		menu.add(menuItem = Utils.createMenuItem("Brush", TOOLS_BRUSH,
				KeyEvent.VK_B, "B", this));
		menu.add(menuItem = Utils.createMenuItem("Pencil", TOOLS_PEN,
				KeyEvent.VK_N, "N", this));
		menu.add(menuItem = Utils.createMenuItem("Magic Wand", TOOLS_MAGICWAND,
				KeyEvent.VK_W, "W", this));
		menu.add(menuItem = Utils.createMenuItem("Set Control Points",
				TOOLS_SETCONTROLPOINTS, KeyEvent.VK_C, "C", this));
		menu.add(menuItem = Utils.createMenuItem("Pointer", TOOLS_POINTER,
				KeyEvent.VK_P, "P", this));
		menu.add(menuItem = Utils.createMenuItem("Pan / Zoom", TOOLS_PANZOOM,
				KeyEvent.VK_Z, "Z", this));
		menuBar.add(menu);
		menu = new JMenu("Layers");
		menu.setMnemonic(KeyEvent.VK_L);
		menu.add(menuItem = Utils.createMenuItem("Add Layer...", ADD_LAYER,
				KeyEvent.VK_A, "", this));
		menu.add(menuItem = Utils.createMenuItem("Remove", REMOVE_LAYER,
				KeyEvent.VK_R, "", this));
		menu.add(menuItem = Utils.createMenuItem("Duplicate", DUPLICATE_LAYER,
				KeyEvent.VK_C, "", this));
		menu.addSeparator();
		menu.add(menuItem = Utils.createMenuItem("Move Up", MOVEUP_LAYER,
				KeyEvent.VK_U, "", this));
		menu.add(menuItem = Utils.createMenuItem("Move Down", MOVEDOWN_LAYER,
				KeyEvent.VK_D, "", this));
		menu.addSeparator();
		menu.add(menuItem = Utils.createMenuItem("Reparent Layer...",
				REPARENT_LAYER, KeyEvent.VK_R, "", this));
		menu.addSeparator();
		menu.add(menuItem = Utils.createMenuItem("Group Layers", GROUP_LAYERS,
				KeyEvent.VK_G, "", this));
		menu.add(menuItem = Utils.createMenuItem("Ungroup Layer",
				UNGROUP_LAYER, KeyEvent.VK_N, "", this));
		menu.addSeparator();
		menu.add(menuItem = Utils.createMenuItem("Join Layers", JOIN_LAYERS,
				KeyEvent.VK_J, "", this));
		menu.add(menuItem = Utils.createMenuItem("Intersect", INTERSECT_LAYERS,
				KeyEvent.VK_I, "", this));
		menu.add(menuItem = Utils.createMenuItem("Subtract", SUBTRACT_LAYERS,
				KeyEvent.VK_S, "", this));
		menu.addSeparator();
		menu.add(menuItem = Utils.createMenuItem("Rename...", RENAME_LAYER,
				KeyEvent.VK_E, "", this));
		menu.addSeparator();
		subMenu = new JMenu("Background Reference");
		subMenu.add(menuItem = Utils.createMenuItem("Set...", BGREF_SET,
				KeyEvent.VK_S, "", this));
		subMenu.add(menuItem = Utils.createMenuItem("Remove", BGREF_REMOVE,
				KeyEvent.VK_R, "", this));
		menu.add(subMenu);
		menuBar.add(menu);
		menu.addSeparator();
		menu.add(menuItem = Utils.createMenuItem("Create Mesh Grid",
				CREATE_MESHGRID, KeyEvent.VK_M, "", this));
		menu.addSeparator();
		menu.add(menuItem = Utils.createCheckBoxMenuItem("See Through",
				SEE_THROUGH, KeyEvent.VK_T, "", this));
		menuItem.setSelected(true);
		menu.add(menuItem = Utils.createCheckBoxMenuItem("Show Mesh",
				SHOW_MESH, KeyEvent.VK_M, "", this));
		menuItem.setSelected(true);
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

	private void open(File file) {
		if (file == null) {
			FileDialog fd = new FileDialog(
					JOptionPane.getFrameForComponent(MeshEditor.this), "Load");
			fd.setVisible(true);
			String file_str = fd.getFile();
			if (file_str == null) {
				return;
			}
			file = new File(fd.getDirectory() + "/" + file_str);
		}
		try {
			StringWriter out = new StringWriter();
			InputStreamReader in = new InputStreamReader(new FileInputStream(
					file));
			Utils.copy(in, out);
			in.close();
			out.close();
			clear();
			JSONObject doc = new JSONObject(out.toString());
			Layer rootLayer = Layer.fromJSON(doc.getJSONObject("rootLayer"));
			layersView.setRootLayer(rootLayer);
			rootLayer.setCanvas(canvas);
			for (Layer layer : rootLayer.getSubLayersRecursively()) {
				layer.setCanvas(canvas);
			}
			JSONObject canvasJSON = doc.optJSONObject("canvas");
			if (canvasJSON != null) {
				canvas.fromJSON(canvasJSON);
			}
			canvas.setCurrLayer(rootLayer);
		} catch (Exception e1) {
			e1.printStackTrace();
			String msg = "An error occurred while trying to load.";
			JOptionPane.showMessageDialog(
					JOptionPane.getFrameForComponent(MeshEditor.this), msg,
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		repaint();
	}

	private boolean canSave() {
		return canvas.canSave();
	}

	private void save(File file) {
		if (!canSave()) {
			return;
		}
		if (file == null) {
			FileDialog fd = new FileDialog(
					JOptionPane.getFrameForComponent(MeshEditor.this), "Save");
			fd.setVisible(true);
			String file_str = fd.getFile();
			if (file_str == null) {
				return;
			}
			file = new File(fd.getDirectory() + "/" + file_str);
		}
		try {
			Layer rootLayer = layersView.getRootLayer();
			for (Layer l : rootLayer.getSubLayersRecursively()) {
				Path path = l.getPath();
				if (path.isFinalized()) {
					Mesh mesh = path.getMesh();
					if (mesh.getControlPointsCount() < 2) {
						String msg = "At least one mesh has less than 2 control points.\n"
								+ "You may not be able to use it for animation. Do you\n"
								+ "still want to continue?";
						if (JOptionPane.showConfirmDialog(this, msg, "Warning",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
							return;
						} else {
							break;
						}
					}
				}
			}
			PrintWriter out = new PrintWriter(new FileOutputStream(file));
			JSONObject doc = new JSONObject();
			doc.put("rootLayer", rootLayer.toJSON());
			doc.put("canvas", canvas.toJSON());
			doc.write(out);
			out.close();
		} catch (Exception e1) {
			e1.printStackTrace();
			String msg = "An error occurred while trying to load.";
			JOptionPane.showMessageDialog(
					JOptionPane.getFrameForComponent(MeshEditor.this), msg,
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		repaint();
	}

	private void toggleOnionSkin() {
		canvas.setSeeThrough(!canvas.isSeeThrough());
	}

	private void toggleShowMesh() {
		canvas.setShowMesh(!canvas.isShowMesh());
	}

	private BufferedImage render() {
		RenderData data = new RenderData();
		Layer rootLayer = layersView.getRootLayer();
		data.canvasSize = new Dimension(canvas.getWidth(), canvas.getHeight());
		data.layer = rootLayer;
		Style style = (Style) rendererSelect.getSelectedItem();
		return RenderImageTask.render(this, style, data, renderImageSettings);
	}

	private void renderSettings() {
		((Style) rendererSelect.getSelectedItem()).showSettings(this);
	}

	private void showImage(Image image) {
		if (image == null) {
			return;
		}
		JDialog d = new JDialog(
				JOptionPane.getFrameForComponent(MeshEditor.this),
				"Image View", true);
		d.setContentPane(new JLabel(new ImageIcon(image)));
		d.pack();
		d.setLocationRelativeTo(d.getParent());
		d.setVisible(true);
		d.dispose();
	}

	private void clear() {
		canvas.clear();
	}

	private void exit() {
		if (JOptionPane.showConfirmDialog(MeshEditor.this,
				"Are you sure you want to exit?", "Exit",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		System.exit(0);
	}

	public CanvasMesh getCanvas() {
		return canvas;
	}

	public CanvasContainer getCanvasContainer() {
		return canvasContainer;
	}

	public static class RenderImageSettings extends Settings {
		public static final String RENDER = "Render";
		@BooleanType(name = "Write Rendered Frame", description = "Whether to write rendered frames to disk or not", category = RENDER)
		public boolean writeRenderedFrame = true;

		@DirectoryType(name = "Rendered Frame Dir", description = "Output directory to write rendered frames", default_ = "/", category = RENDER)
		public File renderedFrameDir = new File("C:/Users/Jasleen/Desktop/");

		@EnumType(name = "Rendered Frame Format", allowed = { "jpg", "png" }, category = RENDER)
		public String renderedFrameFormat = "png";

		@StringType(name = "Rendered Frame Name", category = RENDER)
		public String renderedFrameName = "frame";

		@Override
		public RenderImageSettings clone() {
			RenderImageSettings clone = new RenderImageSettings();
			clone.copyFrom(this);
			return clone;
		}

		@Override
		public void copyFrom(Settings copy) {
			RenderImageSettings s = (RenderImageSettings) copy;
			writeRenderedFrame = s.writeRenderedFrame;
			renderedFrameDir = new File(s.renderedFrameDir.getAbsolutePath());
			renderedFrameFormat = s.renderedFrameFormat;
			renderedFrameName = s.renderedFrameName;
		}

		@Override
		public String[] getCategories() {
			return new String[] { RENDER };
		}
	}

	public static class MeshEditorSettings extends SettingsContainer {
		public static final String GENERAL = "General";

		@IntegerType(name = "Undo Steps", description = "Select the number of Undo steps to preserve while editing", min = 0, max = 50, category = GENERAL)
		public int undoSteps = 10;

		public MeshEditorSettings(Settings... settings) {
			super(settings);
		}

		@Override
		public Settings clone() {
			MeshEditorSettings clone = new MeshEditorSettings(containedClone());
			clone.undoSteps = undoSteps;
			return clone;
		}

		@Override
		public void copyFrom(Settings copy) {
			MeshEditorSettings s = (MeshEditorSettings) copy;
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
		// AlloyLookAndFeel.setProperty("alloy.licenseCode",
		// "2011/06/29#asidjoaisdjoasidjid@mailinator.com#lj8xv#1a193l");
		// AlloyLookAndFeel.setProperty("alloy.isToolbarEffectsEnabled",
		// "false");
		// try {
		// AlloyLookAndFeel alloyLnF = new AlloyLookAndFeel();
		// alloyLnF.setTheme(new GlassTheme(), true);
		// UIManager.setLookAndFeel(alloyLnF);
		// } catch (UnsupportedLookAndFeelException ex) {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		// }
		JFrame f = new JFrame("LiveCanvas - Mesh Editor");
		final MeshEditor meshEditor = new MeshEditor();
		f.setJMenuBar(meshEditor.getMenuBar());
		f.getContentPane().add(meshEditor);
		f.getContentPane().add(meshEditor.getToolBar(), BorderLayout.NORTH);
		f.pack();
		f.setLocationRelativeTo(null);
		f.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
				// meshEditor.exit();
			}
		});
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		f.setVisible(true);
		// meshEditor.open(new File("C:/Users/Jasleen/Desktop/prez.mesh"));
	}
}
