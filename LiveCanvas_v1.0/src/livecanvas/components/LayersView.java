package livecanvas.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import livecanvas.BackgroundRef;
import livecanvas.Canvas;
import livecanvas.Path;
import livecanvas.Settings;
import livecanvas.Utils;
import livecanvas.Utils.ButtonType;
import livecanvas.View;
import livecanvas.components.LayerGroup.CombineOp;

import common.typeutils.AutoPanel;
import common.typeutils.ColorType;
import common.typeutils.EnumType;
import common.typeutils.PropertyFactory;

public class LayersView extends View {
	private static final Class clazz = LayersView.class;

	public static interface Listener {
		public void layerSelectionChanged(Layer selectedLayer);
	}

	public static LayersViewSettings settings;
	// XXX Change later
	public JList layersList;
	private JPanel layerProperties;
	private Layer rootLayer;
	private JPopupMenu popupMenu;

	private List<Listener> listeners = new LinkedList<Listener>();
	private JPanel modifyLayersPanel;

	public LayersView(Layer rootLayer) {
		super(new BorderLayout());
		this.rootLayer = rootLayer;
		modifyLayersPanel = new JPanel();
		modifyLayersPanel.setLayout(new BoxLayout(modifyLayersPanel,
				BoxLayout.X_AXIS));
		modifyLayersPanel
				.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JButton addLayer = Utils.createToolBarButton("Add Layer",
				new ImageIcon(clazz.getResource("res/add_layer.png")),
				ButtonType.ICON_ONLY, ADD_LAYER, "Add Layer", this);
		modifyLayersPanel.add(addLayer);
		JButton remove = Utils.createToolBarButton("Remove", new ImageIcon(
				clazz.getResource("res/remove_layer.png")),
				ButtonType.ICON_ONLY, REMOVE_LAYER, "Remove Layer", this);
		modifyLayersPanel.add(remove);
		modifyLayersPanel.add(Box.createHorizontalStrut(10));
		JButton moveUp = Utils.createToolBarButton("Move Up", new ImageIcon(
				clazz.getResource("res/moveup_layer.png")),
				ButtonType.ICON_ONLY, MOVEUP_LAYER, "Move Layer Up", this);
		modifyLayersPanel.add(moveUp);
		JButton moveDown = Utils.createToolBarButton("Move Down",
				new ImageIcon(clazz.getResource("res/movedown_layer.png")),
				ButtonType.ICON_ONLY, MOVEDOWN_LAYER, "Move Layer Down", this);
		modifyLayersPanel.add(moveDown);
		add(modifyLayersPanel, BorderLayout.NORTH);

		layersList = new JList();
		DefaultListModel model = new DefaultListModel();
		layersList.setModel(model);
		layersList.setCellRenderer(new LayerCellRenderer());
		layersList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		layersList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					showPopup(e.getX(), e.getY());
				}
			}
		});
		layersList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				LayerCell layerCell = (LayerCell) layersList.getSelectedValue();
				if (layerCell == null) {
					return;
				}
				notifyLayerSelectionChanged(layerCell.layer);
			}
		});
		JScrollPane layersListScrollPane = new JScrollPane(layersList);
		layersListScrollPane.setPreferredSize(new Dimension(200, 100));
		add(layersListScrollPane);

		layerProperties = new JPanel(new BorderLayout());
		JScrollPane layerPropertiesScrollPane = new JScrollPane(layerProperties);
		layerPropertiesScrollPane.setPreferredSize(new Dimension(200, 100));
		layerPropertiesScrollPane.setBorder(null);
		add(layerPropertiesScrollPane, BorderLayout.SOUTH);
		updatePropertiesPanel(rootLayer);

		settings = new LayersViewSettings();
		rebuild();
	}

	public LayersViewSettings getSettings() {
		return settings;
	}

	@Override
	public boolean handleEvent(ActionEvent e) {
		switch (Integer.parseInt(e.getActionCommand())) {
		case ADD_LAYER:
			addLayer();
			break;
		case REMOVE_LAYER:
			removeLayer();
			break;
		case DUPLICATE_LAYER:
			duplicateLayer();
			break;
		case MOVEUP_LAYER:
			moveUpLayer();
			break;
		case MOVEDOWN_LAYER:
			moveDownLayer();
			break;
		case REPARENT_LAYER:
			reparentLayer();
			break;
		case GROUP_LAYERS:
			groupLayers();
			break;
		case UNGROUP_LAYER:
			ungroupLayer();
			break;
		case JOIN_LAYERS:
			joinLayers();
			break;
		case INTERSECT_LAYERS:
			intersectLayers();
			break;
		case SUBTRACT_LAYERS:
			subtractLayers();
			break;
		case RENAME_LAYER:
			renameLayer();
			break;
		case BGREF_SET:
			setBgRef();
			break;
		case BGREF_REMOVE:
			removeBgRef();
			break;
		case BGREF_BINDEX:
			backingIndexBgRef();
			break;
		case BGREF_MAKEVISIBLE:
			makeBgRefVisible(true);
			break;
		case BGREF_MAKEINVISIBLE:
			makeBgRefVisible(false);
			break;
		case BGREF_MAKESUBVISIBLE:
			makeSubBgRefVisible(true);
			break;
		case BGREF_MAKESUBINVISIBLE:
			makeSubBgRefVisible(false);
			break;
		case CREATE_MESHGRID:
			createMeshGrid();
			break;
		default:
			return false;
		}
		return true;
	}

	private void showPopup(int x, int y) {
		if (popupMenu == null) {
			popupMenu = createPopupMenu();
		}
		popupMenu.show(layersList, x, y);
	}

	public JPopupMenu createPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		JMenu subMenu;
		JMenuItem menuItem;
		boolean b = isAllowModifyLayers();
		if (b) {
			popupMenu.add(menuItem = Utils.createMenuItem("Duplicate",
					DUPLICATE_LAYER, KeyEvent.VK_C, "", this));
			popupMenu.addSeparator();
			popupMenu.add(menuItem = Utils.createMenuItem("Move Up",
					MOVEUP_LAYER, KeyEvent.VK_U, "", this));
			popupMenu.add(menuItem = Utils.createMenuItem("Move Down",
					MOVEDOWN_LAYER, KeyEvent.VK_D, "", this));
			popupMenu.addSeparator();
			popupMenu.add(menuItem = Utils.createMenuItem("Reparent Layer...",
					REPARENT_LAYER, KeyEvent.VK_R, "", this));
			popupMenu.addSeparator();
			popupMenu.add(menuItem = Utils.createMenuItem("Group Layers",
					GROUP_LAYERS, KeyEvent.VK_G, "", this));
			popupMenu.add(menuItem = Utils.createMenuItem("Ungroup Layer",
					UNGROUP_LAYER, KeyEvent.VK_N, "", this));
			popupMenu.addSeparator();
			popupMenu.add(menuItem = Utils.createMenuItem("Join Layers",
					JOIN_LAYERS, KeyEvent.VK_J, "", this));
			popupMenu.add(menuItem = Utils.createMenuItem("Intersect",
					INTERSECT_LAYERS, KeyEvent.VK_I, "", this));
			popupMenu.add(menuItem = Utils.createMenuItem("Subtract",
					SUBTRACT_LAYERS, KeyEvent.VK_S, "", this));
			popupMenu.addSeparator();
			popupMenu.add(menuItem = Utils.createMenuItem("Rename...",
					RENAME_LAYER, KeyEvent.VK_E, "", this));
			popupMenu.addSeparator();
		}
		subMenu = new JMenu("Background Reference");
		if (b) {
			subMenu.add(menuItem = Utils.createMenuItem("Set...", BGREF_SET,
					KeyEvent.VK_S, "", this));
			subMenu.add(menuItem = Utils.createMenuItem("Remove", BGREF_REMOVE,
					KeyEvent.VK_R, "", this));
			subMenu.addSeparator();
		}
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
		popupMenu.add(subMenu);
		if (b) {
			popupMenu.addSeparator();
			popupMenu.add(menuItem = Utils.createMenuItem("Create Mesh Grid",
					CREATE_MESHGRID, KeyEvent.VK_M, "", this));
		}
		return popupMenu;
	}

	public void addListener(Listener l) {
		listeners.add(l);
	}

	public void removeListener(Listener l) {
		listeners.remove(l);
	}

	protected void notifyLayerSelectionChanged(Layer selectedLayer) {
		updatePropertiesPanel(selectedLayer);
		for (Listener l : listeners) {
			l.layerSelectionChanged(selectedLayer);
		}
	}

	private void updatePropertiesPanel(Layer selectedLayer) {
		layerProperties.removeAll();
		layerProperties.add(new AutoPanel(PropertyFactory
				.createProperties(selectedLayer)));
		layerProperties.revalidate();
		layerProperties.repaint();
	}

	public Layer getSelectedLayer() {
		return ((LayerCell) layersList.getSelectedValue()).layer;
	}

	public void clear() {
		Layer[] subLayers = rootLayer.getSubLayers().toArray(new Layer[0]);
		for (Layer layer : subLayers) {
			removeLayer(layer);
		}
		rebuild();
	}

	public Layer findLayerByName(String name) {
		return findLayerByName(rootLayer, name);
	}

	private Layer findLayerByName(Layer curr, String name) {
		if (curr.name.equals(name)) {
			return curr;
		}
		for (Layer subLayer : curr.getSubLayers()) {
			Layer found = findLayerByName(subLayer, name);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	public void setRootLayer(Layer rootLayer) {
		this.rootLayer = rootLayer;
		rebuild();
	}

	public Layer getRootLayer() {
		return rootLayer;
	}

	private void rebuild() {
		int n = layersList.getSelectedIndex();
		DefaultListModel model = (DefaultListModel) layersList.getModel();
		populate(model, getRootLayer());
		layersList.setSelectedIndex(n >= 0 ? (n < model.getSize() ? n : model
				.getSize() - 1) : 0);
	}

	public void addLayer() {
		String name;
		if ((name = JOptionPane.showInputDialog(LayersView.this, "Enter name:",
				"Add Layer", JOptionPane.PLAIN_MESSAGE)) == null) {
			return;
		}
		addLayer(new Layer(name));
	}

	public void addLayer(Layer layer) {
		if (findLayerByName(getRootLayer(), layer.name) != null) {
			JOptionPane.showMessageDialog(this, "Layer with name \""
					+ layer.name + "\" already exists!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		LayerCell cell = (LayerCell) layersList.getSelectedValue();
		cell.layer.addSubLayer(layer);
		rebuild();
	}

	public void removeLayer() {
		LayerCell cell = (LayerCell) layersList.getSelectedValue();
		removeLayer(cell.layer);
	}

	public void removeLayer(Layer layer) {
		if (layer == getRootLayer()) {
			return;
		}
		layer.getParent().removeSubLayer(layer);
		rebuild();
	}

	public void replaceLayer(Layer replace, Layer with) {
		if (replace == getRootLayer()) {
			return;
		}
		replace.getParent().replaceSubLayer(replace, with);
		rebuild();
	}

	public void duplicateLayer() {
		LayerCell cell = (LayerCell) layersList.getSelectedValue();
		duplicateLayer(cell.layer);
	}

	public void duplicateLayer(Layer layer) {
		if (layer == getRootLayer()) {
			return;
		}
		Layer parent = layer.getParent();
		int index = parent.indexOf(layer);
		parent.addSubLayer(index + 1, layer.clone());
		rebuild();
	}

	public void moveUpLayer() {
		LayerCell cell = (LayerCell) layersList.getSelectedValue();
		moveUpLayer(cell.layer);
	}

	public void moveUpLayer(Layer layer) {
		if (layer == getRootLayer()) {
			return;
		}
		Layer parent = layer.getParent();
		int index = parent.indexOf(layer);
		if (index <= 0) {
			return;
		}
		parent.addSubLayer(index - 1, layer);
		rebuild();
	}

	public void moveDownLayer() {
		LayerCell cell = (LayerCell) layersList.getSelectedValue();
		moveDownLayer(cell.layer);
	}

	public void moveDownLayer(Layer layer) {
		if (layer == getRootLayer()) {
			return;
		}
		Layer parent = layer.getParent();
		int index = parent.indexOf(layer);
		if (index < 0 || index >= parent.getSubLayers().size() - 1) {
			return;
		}
		parent.addSubLayer(index + 1, layer);
		rebuild();
	}

	public void reparentLayer() {
		Layer sel;
		if ((sel = getSelectedLayer()) == rootLayer) {
			return;
		}
		String name;
		if ((name = JOptionPane.showInputDialog(LayersView.this,
				"Enter parent's name:", "Reparent", JOptionPane.PLAIN_MESSAGE)) == null) {
			return;
		}
		if (sel.name.equals(name) || sel.getParent().name.equals(name)) {
			return;
		}
		Layer newParent;
		if ((newParent = findLayerByName(name)) == null) {
			return;
		}
		newParent.addSubLayer(sel);
		rebuild();
	}

	public void groupLayers() {
		LayerCell[] cells;
		if ((cells = getSelectedPair()) == null) {
			return;
		}
		Layer layer1 = cells[0].layer;
		Layer layer2 = cells[1].layer;
		layer2.getParent().removeSubLayer(layer2);
		replaceLayer(
				layer1,
				new LayerGroup(String.format("Group(%s,%s)", layer1.name,
						layer2.name), layer1, layer2, CombineOp.None));
		rebuild();
	}

	private LayerCell[] getSelectedPair() {
		Object[] vals = (Object[]) layersList.getSelectedValues();
		LayerCell[] cells = new LayerCell[2];
		if (vals.length != 2
				|| (cells[0] = (LayerCell) vals[0]).layer.getParent() != (cells[1] = (LayerCell) vals[1]).layer
						.getParent()) {
			JOptionPane.showMessageDialog(this,
					"You must select two sibling layers!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return cells;
	}

	public void ungroupLayer() {
		Layer sel;
		if ((sel = getSelectedLayer()) == rootLayer) {
			return;
		}
		if (sel.isLeaf()) {
			JOptionPane.showMessageDialog(this, "Not a grouped layer!",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Layer parent = sel.getParent();
		parent.removeSubLayer(sel);
		parent.addSubLayer(((LayerGroup) sel).layer1);
		parent.addSubLayer(((LayerGroup) sel).layer2);
		rebuild();
	}

	public void joinLayers() {
		LayerCell[] cells;
		if ((cells = getSelectedPair()) == null) {
			return;
		}
		Layer layer1 = cells[0].layer;
		Layer layer2 = cells[1].layer;
		LayerGroup join;
		try {
			join = new LayerGroup(String.format("Join(%s,%s)", layer1.name,
					layer2.name), layer1, layer2, CombineOp.Join);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Combine operation failed!",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		layer2.getParent().removeSubLayer(layer2);
		replaceLayer(layer1, join);
		rebuild();
	}

	public void intersectLayers() {
		LayerCell[] cells;
		if ((cells = getSelectedPair()) == null) {
			return;
		}
		Layer layer1 = cells[0].layer;
		Layer layer2 = cells[1].layer;
		LayerGroup intersect;
		try {
			intersect = new LayerGroup(String.format("Intersect(%s,%s)",
					layer1.name, layer2.name), layer1, layer2,
					CombineOp.Intersect);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Combine operation failed!",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		layer2.getParent().removeSubLayer(layer2);
		replaceLayer(layer1, intersect);
		rebuild();
	}

	public void subtractLayers() {
		LayerCell[] cells;
		if ((cells = getSelectedPair()) == null) {
			return;
		}
		Layer layer1 = cells[0].layer;
		Layer layer2 = cells[1].layer;
		LayerGroup subtract;
		try {
			subtract = new LayerGroup(String.format("Subtract(%s,%s)",
					layer1.name, layer2.name), layer1, layer2,
					CombineOp.Subtract);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Combine operation failed!",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		layer2.getParent().removeSubLayer(layer2);
		replaceLayer(layer1, subtract);
		rebuild();
	}

	public void renameLayer() {
		Layer sel;
		if ((sel = getSelectedLayer()) == rootLayer) {
			return;
		}
		String name;
		if ((name = JOptionPane.showInputDialog(LayersView.this, "Enter name:",
				"Rename Layer", JOptionPane.PLAIN_MESSAGE)) == null) {
			return;
		}
		if (findLayerByName(getRootLayer(), name) != null) {
			JOptionPane.showMessageDialog(this, "Layer with name \"" + name
					+ "\" already exists!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		sel.setName(name);
		rebuild();
	}

	public void setBgRef() {
		Layer sel;
		if ((sel = getSelectedLayer()) == rootLayer) {
			return;
		}
		JRadioButton[] types = { new JRadioButton("Image"),
				new JRadioButton("Color") };
		types[0].setSelected(true);
		JPanel bgrefTypes = new JPanel(new GridLayout(types.length, 1, 5, 5));
		ButtonGroup bg = new ButtonGroup();
		for (JRadioButton type : types) {
			bgrefTypes.add(type);
			bg.add(type);
		}
		if (JOptionPane.showConfirmDialog(this, bgrefTypes,
				"Background Reference", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION) {
			return;
		}
		BackgroundRef bgref;
		if (types[0].isSelected()) {
			bgref = new BackgroundRef.BGImage();
		} else if (types[1].isSelected()) {
			bgref = new BackgroundRef.BGColor();
		} else {
			return;
		}
		Canvas canvas = sel.getCanvas();
		if (!bgref.load(this,
				new Dimension(canvas.getWidth(), canvas.getHeight()))) {
			return;
		}
		sel.setBackgroundRef(bgref);
	}

	public void removeBgRef() {
		Layer sel;
		if ((sel = getSelectedLayer()) == rootLayer) {
			return;
		}
		sel.setBackgroundRef(null);
	}

	public void backingIndexBgRef() {
		Layer sel;
		if ((sel = getSelectedLayer()) == rootLayer) {
			return;
		}
		BackgroundRef bgref = sel.getBackgroundRef();
		if (bgref == null) {
			String msg = "Please set a background reference first!";
			JOptionPane.showMessageDialog(this, msg, "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		String[] values = new String[bgref.getBackingCount()];
		for (int i = 0; i < values.length; i++) {
			values[i] = "" + i;
		}
		JComboBox cbo = new JComboBox(new DefaultComboBoxModel(values));
		cbo.setSelectedIndex(bgref.getBackingIndex());
		if (JOptionPane.showConfirmDialog(this, cbo, "Backing Index",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
			return;
		}
		bgref.setBackingIndex(Integer.parseInt((String) cbo.getSelectedItem()));
		notifyLayerSelectionChanged(getSelectedLayer());
	}

	public void makeBgRefVisible(boolean visible) {
		Layer sel;
		if ((sel = getSelectedLayer()) == rootLayer) {
			return;
		}
		BackgroundRef bgref = sel.getBackgroundRef();
		if (bgref == null) {
			String msg = "Please set a background reference first!";
			JOptionPane.showMessageDialog(this, msg, "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		sel.setBackgroundRefVisible(visible);
	}

	public void makeSubBgRefVisible(boolean visible) {
		Layer sel = getSelectedLayer();
		for (Layer l : sel.getSubLayers()) {
			BackgroundRef bgref = l.getBackgroundRef();
			if (bgref != null) {
				l.setBackgroundRefVisible(visible);
			}
		}
	}

	public void createMeshGrid() {
		Layer sel;
		if ((sel = getSelectedLayer()) == rootLayer) {
			return;
		}
		if (sel.getBackgroundRef() == null) {
			String msg = "You must set the background reference for this layer first";
			JOptionPane.showMessageDialog(LayersView.this, msg, "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		sel.setPath(Path.fromBackgroundRef(sel.getBackgroundRef(),
				LayersViewSettings
						.meshDensity2ParticlesBBoxRatio(settings.meshDensity)));
	}

	public boolean isAllowModifyLayers() {
		return modifyLayersPanel.isVisible();
	}

	public void setAllowModifyLayers(boolean b) {
		modifyLayersPanel.setVisible(b);
	}

	private static class LayerCell {
		public Layer layer;
		public int depth;

		public LayerCell(Layer layer, int depth) {
			this.layer = layer;
			this.depth = depth;
		}
	}

	private void populate(DefaultListModel model, Layer root) {
		model.clear();
		populate(model, root, 0);
	}

	private void populate(DefaultListModel model, Layer layer, int depth) {
		model.addElement(new LayerCell(layer, depth));
		for (Layer sub : layer.getSubLayers()) {
			populate(model, sub, depth + 1);
		}
	}

	private static class LayerCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			LayerCell cell = (LayerCell) value;
			JLabel lbl = (JLabel) super.getListCellRendererComponent(list,
					value, index, isSelected, cellHasFocus);
			lbl.setIcon(getIconWithWidth(cell.depth * 20));
			lbl.setText(cell.layer.name);
			return lbl;
		}

		private Map<Integer, Icon> map = new HashMap<Integer, Icon>();

		private Icon getIconWithWidth(int leftMargin) {
			Icon b = map.get(leftMargin);
			if (b != null) {
				return b;
			}
			b = new ImageIcon(new BufferedImage(1 + leftMargin, 20,
					BufferedImage.TYPE_INT_ARGB));
			map.put(leftMargin, b);
			return b;
		}
	}

	public static class LayersViewSettings extends Settings {
		public static final String LAYERS = "Layers";

		public static final String DENSITY_VERYLOW = "Very Low",
				DENSITY_LOW = "Low", DENSITY_MEDIUM = "Medium",
				DENSITY_HIGH = "High", DENSITY_VERYHIGH = "Very High";
		@EnumType(name = "Mesh Density", allowed = { DENSITY_VERYLOW,
				DENSITY_LOW, DENSITY_MEDIUM, DENSITY_HIGH, DENSITY_VERYHIGH }, category = LAYERS)
		public String meshDensity = DENSITY_MEDIUM;

		private static final Map<String, Double> meshDensity2ParticlesBBoxRatio = new HashMap<String, Double>();
		static {
			meshDensity2ParticlesBBoxRatio.put(DENSITY_VERYLOW, 1 / 5000.0);
			meshDensity2ParticlesBBoxRatio.put(DENSITY_LOW, 1 / 2500.0);
			meshDensity2ParticlesBBoxRatio.put(DENSITY_MEDIUM, 1 / 1000.0);
			meshDensity2ParticlesBBoxRatio.put(DENSITY_HIGH, 1 / 500.0);
			meshDensity2ParticlesBBoxRatio.put(DENSITY_VERYHIGH, 1 / 250.0);
		}

		public static double meshDensity2ParticlesBBoxRatio(String density) {
			return meshDensity2ParticlesBBoxRatio.get(density);
		}

		@ColorType(name = "Mesh Color", category = LAYERS)
		public Color meshColor = Color.gray;

		@Override
		public LayersViewSettings clone() {
			LayersViewSettings clone = new LayersViewSettings();
			clone.meshDensity = meshDensity;
			clone.meshColor = meshColor;
			return clone;
		}

		@Override
		public void copyFrom(Settings copy) {
			LayersViewSettings s = (LayersViewSettings) copy;
			meshDensity = s.meshDensity;
			meshColor = s.meshColor;
		}

		@Override
		public String[] getCategories() {
			return new String[] { LAYERS };
		}
	}

	public static void main(String[] args) throws Exception {
		JDialog d = new JDialog((JFrame) null, "LayersView Test", true);
		Layer root = new Layer("ROOT");
		// root.addSubLayer(new Layer("Child 0"));
		// root.addSubLayer(new Layer("Child 1"));
		d.setContentPane(new LayersView(root));
		d.pack();
		d.setLocationRelativeTo(null);
		d.setVisible(true);
		System.exit(0);
	}
}
