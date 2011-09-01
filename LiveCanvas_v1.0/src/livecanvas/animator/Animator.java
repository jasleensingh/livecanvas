package livecanvas.animator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import livecanvas.BackgroundRef;
import livecanvas.CanvasContainer;
import livecanvas.CanvasMesh;
import livecanvas.ControlPoint;
import livecanvas.Mesh;
import livecanvas.Path;
import livecanvas.Perspective;
import livecanvas.RenderKeyframesTask;
import livecanvas.Settings;
import livecanvas.Settings.SettingsContainer;
import livecanvas.Tool;
import livecanvas.Tool.Pointer.PointerHandler;
import livecanvas.Utils;
import livecanvas.Utils.ButtonType;
import livecanvas.Vec3;
import livecanvas.components.Keyframe;
import livecanvas.components.Layer;
import livecanvas.components.LayersView;
import livecanvas.components.Viewpoint;
import livecanvas.image.RenderData;
import livecanvas.image.Style;

import org.json.JSONObject;

import common.typeutils.AutoPanel;
import common.typeutils.IntegerType;
import common.typeutils.PropertyFactory;

public class Animator extends Perspective implements KeyframesContainer,
		Tool.ToolContext, CanvasMesh.Listener, Timeline.Listener,
		LayersView.Listener {
	private static final Class clazz = Animator.class;

	private JToolBar toolBar;
	private JMenuBar menuBar;
	private AnimatorSettings settings;
	private PreviewPanel preview;
	private Timeline timeline;
	private OnionSkin onionSkin;

	private Color selectedColor;
	private JToolBar tools;
	private CanvasMesh canvas;
	private CanvasContainer canvasContainer;
	private LayersView layersView;
	private JPanel propertiesPanel;
	private JComboBox rendererSelect;

	private int prevX, prevY;
	private int currX, currY;
	private Vertex overControlPointVertex;
	private PointerHandler pointerHandler = new PointerHandler() {
		public boolean onSelected() {
			if (!canvas.getCurrLayer().getPath().isFinalized()) {
				JOptionPane.showMessageDialog(Animator.this,
						"Please create a mesh first", "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
		}

		public void mousePressed(MouseEvent e) {
			overControlPointVertex = null;
			Mesh mesh = canvas.getCurrLayer().getPath().getMesh();
			Vertex v = mesh.findNearestMeshVertex(e.getX(), e.getY());
			for (ControlPoint cp : mesh.getControlPoints()) {
				if (mesh.getControlPointVertex(cp) == v) {
					overControlPointVertex = v;
					break;
				}
			}
			if (overControlPointVertex == null) {
				return;
			}
			prevX = currX = e.getX();
			prevY = currY = e.getY();
		}

		public void mouseDragged(MouseEvent e) {
			if (overControlPointVertex == null) {
				return;
			}
			currX = e.getX();
			currY = e.getY();
			rigidTransform(currX - prevX, currY - prevY);
			prevX = currX;
			prevY = currY;
			canvas.fireMeshEdit(MESH_MANIPULATE);
			repaint();
		}

		private void rigidTransform(int dx, int dy) {
			Mesh mesh = canvas.getCurrLayer().getPath().getMesh();
			if (mesh.getControlPoints().size() <= 1) {
				for (Vertex v : mesh.vertices) {
					v.x += dx;
					v.y += dy;
				}
			} else {
				overControlPointVertex.translate(dx, dy, 0);
				canvas.updateTransform();
			}
		}
	};

	public Animator() {
		super("Animator", new ImageIcon(clazz.getResource("res/animator.png")),
				new BorderLayout());
		setPreferredSize(new Dimension(1024, 600));
		setBackground(Color.darkGray);

		tools = createTools();
		add(tools, BorderLayout.WEST);

		selectedColor = Color.black;
		JPanel east = new JPanel(new BorderLayout());
		east.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		layersView = new LayersView(new Layer("ROOT"));
		chain(layersView);
		layersView.addListener(this);
		layersView.setAllowModifyLayers(false);
		east.add(layersView, BorderLayout.NORTH);
		propertiesPanel = new JPanel(new BorderLayout());
		east.add(propertiesPanel);
		add(east, BorderLayout.EAST);

		JPanel center = new JPanel(new BorderLayout());
		center.setBackground(Color.lightGray);
		canvas = new CanvasMesh(800, 600, pointerHandler);
		canvas.setCurrLayer(getRootLayer());
		canvas.addListener(this);
		canvasContainer = new CanvasContainer(canvas);
		center.add(canvasContainer);
		add(center);

		settings = new AnimatorSettings(canvas.getSettings(),
				layersView.getSettings());

		onionSkin = new OnionSkin(this, canvas);
		canvas.setOnionSkin(onionSkin);

		timeline = new Timeline();
		chain(timeline);
		timeline.addListener(this);
		timeline.addListener(onionSkin);
		add(timeline, BorderLayout.SOUTH);

		setSelectedToolType(TOOLS_PANZOOM);

		// importMesh(new File(
		// "C:/Users/Jasleen/Desktop/livecanvas_bgrefs/large/s/el2.mesh"));
		// layersView.layersList.setSelectedIndex(3);
		// setSelectedToolType(TOOLS_POINTER);
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
		case IMPORT_MESH:
			importMesh(null);
			break;
		case SETTINGS:
			showSettingsDialog();
			break;
		case RENDER_SETTINGS:
			renderSettings();
			break;
		case PREVIEW:
			preview();
			break;
		case SEE_THROUGH:
			toggleSeeThrough();
			break;
		case SHOW_MESH:
			toggleShowMesh();
			break;
		case TOOLS_PEN:
			setSelectedToolType(TOOLS_PEN);
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

	public AnimatorSettings getSettings() {
		return settings;
	}

	@Override
	public void meshEdited(int mode, Layer layer) {
		switch (mode) {
		case MESH_MANIPULATE:
			timeline.meshEdited(layer);
			break;
		}
		repaint();
	}

	private void open(File file) {
		if (file == null) {
			FileDialog fd = new FileDialog(
					JOptionPane.getFrameForComponent(Animator.this), "Load");
			fd.setVisible(true);
			String file_str = fd.getFile();
			if (file_str == null) {
				return;
			}
			file = new File(fd.getDirectory() + "/" + file_str);
		}
		try {
			FileInputStream in = new FileInputStream(file);
			StringWriter sw = new StringWriter();
			InputStreamReader isr = new InputStreamReader(in);
			Utils.copy(isr, sw);
			clear();
			JSONObject doc = new JSONObject(sw.toString());
			Layer rootLayer = Layer.fromJSON(doc.getJSONObject("rootLayer"));
			layersView.setRootLayer(rootLayer);
			rootLayer.setCanvas(canvas);
			for (Layer layer : rootLayer.getSubLayersRecursively()) {
				layer.setCanvas(canvas);
			}
			canvas.setCurrLayer(rootLayer);
			canvas.initializeTransform();
			Keyframes keyframes = Keyframes.fromJSON(doc
					.getJSONObject("keyframes"));
			for (Keyframe kf : keyframes) {
				updateMeshFromKeyframe(kf);
			}
			System.err.println(keyframes.size() + " keyframes");
			timeline.setKeyframes(keyframes);
			in.close();
			timeline.setKeyframeSelected(keyframes.get(0));
		} catch (Exception e1) {
			e1.printStackTrace();
			String msg = "An error occurred while trying to load.";
			JOptionPane.showMessageDialog(
					JOptionPane.getFrameForComponent(Animator.this), msg,
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		repaint();
	}

	@Override
	public void timelineChanged(int event) {
		switch (event) {
		case Timeline.Listener.ONIONSKIN:
			onionSkin.setShowPrevious(timeline.isOnionSkinPrev());
			onionSkin.setShowNext(timeline.isOnionSkinNext());
			break;
		}
		repaint();
	}

	@Override
	public void keyframeChanged(Keyframe keyframe, int event) {
		switch (event) {
		case Timeline.Listener.ADDED:
			keyframe.meshEdited(canvas.getCurrLayer());
			timeline.setKeyframeSelected(keyframe);
			break;
		case Timeline.Listener.REMOVED:
			break;
		case Timeline.Listener.SELECTED:
			updateMeshFromKeyframe(keyframe);
			break;
		}
		repaint();
	}

	@Override
	public Keyframes getKeyframes() {
		return timeline.getKeyframes();
	}

	@Override
	public Keyframe getKeyframeSelected() {
		return timeline.getKeyframeSelected();
	}

	public void updateMeshFromKeyframe(Keyframe kf) {
		Layer rootLayer = canvas.getCurrLayer().getRoot();
		updateMeshFromKeyframe(kf, rootLayer);
		canvas.updateTransform();
		kf.meshEdited(rootLayer);
	}

	private void updateMeshFromKeyframe(Keyframe kf, Layer layer) {
		Viewpoint[][] viewpoints = layer.getViewpoints();
		for (int vx = 0; vx < viewpoints.length; vx++) {
			for (int vy = 0; vy < viewpoints[vx].length; vy++) {
				Path path = viewpoints[vx][vy].getPath();
				if (path.isFinalized()) {
					Mesh mesh = path.getMesh();
					int count = mesh.getControlPointsCount();
					Vec3[] cpvLocations = kf.getControlPointVertexLocations(
							layer, vx, vy);
					for (int i = 0; i < count; i++) {
						mesh.getControlPointVertex(i).set(cpvLocations[i]);
					}
					BackgroundRef bgref = layer.getBackgroundRef();
					if (bgref != null) {
						int index = kf.getBackgroundRefBackingIndex(layer, vx,
								vy);
						if (index >= 0) {
							bgref.setBackingIndex(index);
						}
					}
				}
			}
		}
		for (Layer subLayer : layer.getSubLayers()) {
			updateMeshFromKeyframe(kf, subLayer);
		}
	}

	@Override
	public void updateMeshFromKeyframes(Keyframe kf1, Keyframe kf2,
			float interpolation) {
		interpolation = Math.max(0, Math.min(1, interpolation));
		Layer rootLayer = canvas.getCurrLayer().getRoot();
		updateMeshFromKeyframes(kf1, kf2, interpolation, rootLayer);
		canvas.updateTransform();
		// kf2.meshEdited(rootLayer);
	}

	// XXX: Check viewpoint code, haven't updated that. 07/20
	private void updateMeshFromKeyframes(Keyframe kf1, Keyframe kf2,
			float interpolation, Layer layer) {
		Viewpoint[][] viewpoints = layer.getViewpoints();
		for (int vx = 0; vx < viewpoints.length; vx++) {
			for (int vy = 0; vy < viewpoints[vx].length; vy++) {
				Path path = viewpoints[vx][vy].getPath();
				if (path.isFinalized()) {
					Mesh mesh = path.getMesh();
					int count = mesh.getControlPointsCount();
					Vec3[] cpvLocations1 = kf1.getControlPointVertexLocations(
							layer, vx, vy);
					Vec3[] cpvLocations2 = kf2.getControlPointVertexLocations(
							layer, vx, vy);
					Vec3 cpvLoc = new Vec3();
					for (int i = 0; i < count; i++) {
						Vec3 v1 = cpvLocations1[i];
						Vec3 v2 = cpvLocations2[i];
						cpvLoc.set(v1.x + interpolation * (v2.x - v1.x), v1.y
								+ interpolation * (v2.y - v1.y), v1.z
								+ interpolation * (v2.z - v1.z));
						mesh.getControlPointVertex(i).set(cpvLoc);
					}
				}
			}
		}
		for (Layer subLayer : layer.getSubLayers()) {
			updateMeshFromKeyframes(kf1, kf2, interpolation, subLayer);
		}
	}

	private void save(File file) {
		if (!canvas.canSave()) {
			return;
		}
		if (file == null) {
			FileDialog fd = new FileDialog(
					JOptionPane.getFrameForComponent(Animator.this), "Save");
			fd.setVisible(true);
			String file_str = fd.getFile();
			if (file_str == null) {
				return;
			}
			file = new File(fd.getDirectory() + "/" + file_str);
		}
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(file));
			JSONObject doc = new JSONObject();
			doc.put("rootLayer", layersView.getRootLayer().toJSON());
			Keyframes keyframes = getKeyframes();
			doc.put("keyframes", keyframes.toJSON());
			doc.write(out);
			out.close();
		} catch (Exception e1) {
			e1.printStackTrace();
			String msg = "An error occurred while trying to load.";
			JOptionPane.showMessageDialog(
					JOptionPane.getFrameForComponent(Animator.this), msg,
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		repaint();
	}

	private void clear() {
		layersView.clear();
		canvas.clear();
	}

	private void exit() {
		if (JOptionPane.showConfirmDialog(Animator.this,
				"Are you sure you want to exit?", "Exit",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		System.exit(0);
	}

	private void importMesh(File file) {
		if (file == null) {
			FileDialog fd = new FileDialog(
					JOptionPane.getFrameForComponent(Animator.this),
					"Import Mesh");
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
			canvas.setCurrLayer(rootLayer);
			canvas.initializeTransform();
			if (getKeyframes().size() <= 0) {
				timeline.addKeyframe(new Keyframe(0));
			}
			// canvas.fireMeshEdit(MESH_MANIPULATE);
		} catch (Exception e1) {
			e1.printStackTrace();
			String msg = "An error occurred while trying to load.";
			JOptionPane.showMessageDialog(
					JOptionPane.getFrameForComponent(Animator.this), msg,
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void renderSettings() {
		((Style) rendererSelect.getSelectedItem()).showSettings(this);
	}

	private void preview() {
		showFrames(render());
	}

	private void showFrames(BufferedImage[] renderedFrames) {
		if (renderedFrames == null) {
			return;
		}
		final JDialog f = new JDialog((JFrame) null, "Preview", true);
		preview = new PreviewPanel(canvas, renderedFrames);
		f.getContentPane().add(preview);
		f.pack();
		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		f.setVisible(true);
		preview.stop();
		preview = null;
		f.dispose();
	}

	private BufferedImage[] render() {
		Keyframes keyframes = getKeyframes();
		Style style = (Style) rendererSelect.getSelectedItem();
		Layer rootLayer = getRootLayer();
		// Warn if style doesn't support blending
		if (!style.renderer.supportsBlending()) {
			for (Layer l : rootLayer.getSubLayersRecursively()) {
				BackgroundRef bgref = l.getBackgroundRef();
				if (bgref != null && bgref.getBackingCount() > 1) {
					String msg = "The selected style doesn't support blending.\n"
							+ "Do you still want to continue?";
					if (JOptionPane.showConfirmDialog(this, msg, "Warning",
							JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
						return null;
					}
				}
			}
		}
		RenderData data = new RenderData();
		data.canvasSize = new Dimension(canvas.getWidth(), canvas.getHeight());
		data.layer = rootLayer;
		data.keyframes = keyframes;
		return RenderKeyframesTask.render(this, this, keyframes, style, data);
	}

	private void toggleSeeThrough() {
		canvas.setSeeThrough(!canvas.isSeeThrough());
	}

	private void toggleShowMesh() {
		canvas.setShowMesh(!canvas.isShowMesh());
	}

	public Color getSelectedColor() {
		return selectedColor;
	}

	@Override
	public void layerSelectionChanged(Layer selectedLayer) {
		canvas.setCurrLayer(selectedLayer);
		// notify as bgref backing index may have changed
		Keyframe sel = getKeyframeSelected();
		if (sel != null) {
			sel.meshEdited(selectedLayer);
		}
		repaint();
	}

	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		JButton button;
		Class clazz = Animator.class;
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
		toolbar.add(rendererSelectContainer);
		toolbar.add(Utils.createToolBarButton("Render Settings", new ImageIcon(
				clazz.getResource("res/render_settings.png")),
				ButtonType.ICON_ONLY, RENDER_SETTINGS, "Render Settings", this));
		toolbar.add(Utils.createToolBarButton("Preview",
				new ImageIcon(clazz.getResource("res/preview.png")),
				ButtonType.ICON_ONLY, PREVIEW, "Preview animation", this));
		return toolbar;
	}

	private JToolBar createTools() {
		JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
		Class clazz = Animator.class;
		ButtonGroup toolsBg = new ButtonGroup();
		JToggleButton toolsButton;
		toolbar.add(toolsButton = Utils.createToolBarToggleButton("Pointer",
				new ImageIcon(clazz.getResource("res/pointer.png")),
				ButtonType.ICON_ONLY, TOOLS_POINTER, "Pointer", toolsBg, this));
		toolbar.add(toolsButton = Utils.createToolBarToggleButton("PanZoom",
				new ImageIcon(clazz.getResource("res/panzoom.png")),
				ButtonType.ICON_ONLY, TOOLS_PANZOOM, "Pan or Zoom Canvas",
				toolsBg, this));
		toolsButton.setSelected(true);
		return toolbar;
	}

	private JMenuBar createMenuBar() {
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
		menu.add(menuItem = Utils.createMenuItem("Import Mesh...", IMPORT_MESH,
				KeyEvent.VK_M, "", this));
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
		menu.add(menuItem = Utils.createMenuItem("Pointer", TOOLS_POINTER,
				KeyEvent.VK_P, "P", this));
		menu.add(menuItem = Utils.createMenuItem("Pan / Zoom", TOOLS_PANZOOM,
				KeyEvent.VK_Z, "Z", this));
		menuBar.add(menu);
		menu = new JMenu("Layers");
		menu.setMnemonic(KeyEvent.VK_L);
		menu.add(menuItem = Utils.createCheckBoxMenuItem("See Through",
				SEE_THROUGH, KeyEvent.VK_T, "", this));
		menuItem.setSelected(true);
		menu.add(menuItem = Utils.createCheckBoxMenuItem("Show Mesh",
				SHOW_MESH, KeyEvent.VK_M, "", this));
		menuItem.setSelected(true);
		menuBar.add(menu);
		menu = new JMenu("Timeline");
		menu.setMnemonic(KeyEvent.VK_M);
		menu.add(menuItem = Utils.createMenuItem("Add Keyframe", ADD_KEYFRAME,
				KeyEvent.VK_A, "", this));
		menu.add(menuItem = Utils.createMenuItem("Remove Keyframe",
				REMOVE_KEYFRAME, KeyEvent.VK_R, "", this));
		menu.addSeparator();
		menu.add(menuItem = Utils.createMenuItem("Select prev",
				SELECTPREV_KEYFRAME, KeyEvent.VK_P, "A", this));
		menu.add(menuItem = Utils.createMenuItem("Select next",
				SELECTNEXT_KEYFRAME, KeyEvent.VK_N, "D", this));
		menu.addSeparator();
		subMenu = new JMenu("Background Reference");
		subMenu.add(menuItem = Utils.createMenuItem("Make Visible",
				BGREF_MAKEVISIBLE, KeyEvent.VK_V, "", this));
		subMenu.add(menuItem = Utils.createMenuItem("Make Invisible",
				BGREF_MAKEINVISIBLE, KeyEvent.VK_I, "", this));
		subMenu.add(menuItem = Utils.createMenuItem("Make Sub-layers Visible",
				BGREF_MAKESUBVISIBLE, KeyEvent.VK_V, "", this));
		subMenu.add(menuItem = Utils.createMenuItem(
				"Make Sub-layers Invisible", BGREF_MAKESUBINVISIBLE,
				KeyEvent.VK_I, "", this));
		subMenu.addSeparator();
		subMenu.add(menuItem = Utils.createMenuItem("Backing Index...",
				BGREF_BINDEX, KeyEvent.VK_X, "", this));
		menu.add(subMenu);
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

	public CanvasMesh getCanvas() {
		return canvas;
	}

	public CanvasContainer getCanvasContainer() {
		return canvasContainer;
	}

	public static class AnimatorSettings extends SettingsContainer {
		public static final String GENERAL = "General";
		public static final String EDITING = "Editing";

		@IntegerType(name = "Undo Steps", description = "Number of Undo steps to preserve while editing", min = 0, max = 50, category = GENERAL)
		public int undoSteps = 10;

		@IntegerType(name = "Snap Grid Size", description = "Snap Grid Size", min = 1, max = 50, category = EDITING)
		public int snapGridSize = 10;

		public AnimatorSettings(Settings... settings) {
			super(settings);
		}

		@Override
		public Settings clone() {
			AnimatorSettings clone = new AnimatorSettings(containedClone());
			clone.undoSteps = undoSteps;
			return clone;
		}

		@Override
		public void copyFrom(Settings copy) {
			AnimatorSettings s = (AnimatorSettings) copy;
			undoSteps = s.undoSteps;
			containedCopyFrom(s);
		}

		@Override
		public String[] getCategories() {
			List<String> list = new LinkedList<String>();
			list.add(GENERAL);
			list.add(EDITING);
			list.addAll(Arrays.asList(containedCategories()));
			return list.toArray(new String[0]);
		}
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		JFrame f = new JFrame("LiveCanvas - Animator");
		final Animator animator = new Animator();
		f.setJMenuBar(animator.getMenuBar());
		f.getContentPane().add(animator);
		f.getContentPane().add(animator.getToolBar(), BorderLayout.NORTH);
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
