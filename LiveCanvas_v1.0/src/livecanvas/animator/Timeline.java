package livecanvas.animator;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import livecanvas.Utils;
import livecanvas.Utils.ButtonType;
import livecanvas.View;
import livecanvas.components.InterpolationEditor;
import livecanvas.components.Keyframe;
import livecanvas.components.KeyframeView;
import livecanvas.components.Layer;

public class Timeline extends View implements KeyframeView.Listener {
	private static Class clazz = Timeline.class;

	public static interface Listener {
		public static int ADDED = 0, REMOVED = 1, SELECTED = 2, ONIONSKIN = 20;

		public void timelineChanged(int event);

		public void keyframeChanged(Keyframe keyframe, int event);
	}

	private KeyframeView keyframeView;
	private JScrollPane scrollPane;
	private List<Listener> listeners;
	private boolean onionSkinPrev;
	private boolean onionSkinNext;

	public Timeline() {
		super(new BorderLayout());
		this.listeners = new LinkedList<Timeline.Listener>();
		JButton button;
		JToggleButton toggleButton;
		JPanel north = new JPanel();
		north.setLayout(new BoxLayout(north, BoxLayout.X_AXIS));
		north.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		north.add(button = Utils.createToolBarButton("Add Keyframe",
				new ImageIcon(clazz.getResource("res/add_keyframe.png")),
				ButtonType.ICON_ONLY, ADD_KEYFRAME, "Add a keyframe", this));
		north.add(button = Utils.createToolBarButton("Remove", new ImageIcon(
				clazz.getResource("res/remove_keyframe.png")),
				ButtonType.ICON_ONLY, REMOVE_KEYFRAME,
				"Remove selected keyframe", this));
		north.add(Box.createHorizontalStrut(20));
		north.add(button = Utils.createToolBarButton("Move to previous",
				new ImageIcon(clazz.getResource("res/moveprev_keyframe.png")),
				ButtonType.ICON_ONLY, MOVEPREV_KEYFRAME, "Move to previous",
				this));
		north.add(button = Utils.createToolBarButton("Move to next",
				new ImageIcon(clazz.getResource("res/movenext_keyframe.png")),
				ButtonType.ICON_ONLY, MOVENEXT_KEYFRAME, "Move to next", this));
		north.add(Box.createHorizontalStrut(20));
		north.add(toggleButton = Utils.createToolBarToggleButton(
				"Show Previous Frame",
				new ImageIcon(clazz.getResource("res/onionskin_prev.png")),
				ButtonType.ICON_ONLY, ONIONSKIN_PREV, "Show Previous Frame",
				null, this));
		north.add(toggleButton = Utils.createToolBarToggleButton(
				"Show Next Frame",
				new ImageIcon(clazz.getResource("res/onionskin_next.png")),
				ButtonType.ICON_ONLY, ONIONSKIN_NEXT, "Show Next Frame", null,
				this));
		add(north, BorderLayout.NORTH);
		keyframeView = new KeyframeView(new Keyframes(), this);
		keyframeView.updateSize();
		scrollPane = new JScrollPane(keyframeView);
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		add(scrollPane);
	}

	@Override
	public boolean handleEvent(ActionEvent e) {
		switch (Integer.parseInt(e.getActionCommand())) {
		case ADD_KEYFRAME:
			addKeyframe(new Keyframe(getKeyframes().size()));
			break;
		case REMOVE_KEYFRAME:
			removeKeyframe(getKeyframeSelected());
			break;
		case SELECTPREV_KEYFRAME:
			setKeyframeSelected(getKeyframeSelectedIndex() - 1);
			break;
		case SELECTNEXT_KEYFRAME:
			setKeyframeSelected(getKeyframeSelectedIndex() + 1);
			break;
		case MOVEPREV_KEYFRAME:
			movePrevKeyframe(getKeyframeSelected());
			break;
		case MOVENEXT_KEYFRAME:
			moveNextKeyframe(getKeyframeSelected());
			break;
		case ONIONSKIN_PREV:
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	public void onKeyframeSelected(Keyframe kf) {
		setKeyframeSelected(kf);
	}

	@Override
	public void onInterpolatorSelected(Interpolator in) {
		new InterpolationEditor(this, in).setVisible(true);
	}

	public void addListener(Listener l) {
		listeners.add(l);
	}

	public void removeListener(Listener l) {
		listeners.remove(l);
	}

	protected void notifyTimelineChanged(int event) {
		for (Listener l : listeners) {
			l.timelineChanged(event);
		}
	}

	protected void notifyKeyframeChanged(Keyframe kf, int event) {
		for (Listener l : listeners) {
			l.keyframeChanged(kf, event);
		}
	}

	public Keyframes getKeyframes() {
		return keyframeView.getKeyframes();
	}

	public void setKeyframes(Keyframes keyframes) {
		keyframeView.setKeyframes(keyframes);
	}

	public Keyframe getKeyframeSelected() {
		return Keyframe.selectedKeyframe;
	}

	public int getKeyframeSelectedIndex() {
		return keyframeView.getKeyframes().indexOf(getKeyframeSelected());
	}

	public void setKeyframeSelected(int index) {
		Keyframes keyframes = keyframeView.getKeyframes();
		if (index < 0 || index >= keyframes.size()) {
			return;
		}
		setKeyframeSelected(keyframes.get(index));
	}

	public void setKeyframeSelected(Keyframe kf) {
		if (kf == getKeyframeSelected()) {
			return;
		}
		kf.setSelected(true);
		repaint();
		notifyKeyframeChanged(kf, Listener.SELECTED);
	}

	public void meshEdited(Layer layer) {
		getKeyframeSelected().meshEdited(layer);
	}

	public void addKeyframe(Keyframe kf) {
		Keyframe sel = getKeyframeSelected();
		Keyframes keyframes = getKeyframes();
		if (sel == null) {
			keyframes.add(kf);
		} else {
			keyframes.add(keyframes.indexOf(sel) + 1, kf);
		}
		keyframeView.updateSize();
		scrollPane.getViewport().revalidate();
		repaint();
		notifyKeyframeChanged(kf, Listener.ADDED);
	}

	public void removeKeyframe(Keyframe kf) {
		Keyframes keyframes = getKeyframes();
		int index = keyframes.indexOf(kf);
		keyframes.remove(kf);
		keyframeView.updateSize();
		scrollPane.getViewport().revalidate();
		repaint();
		notifyKeyframeChanged(kf, Listener.REMOVED);
		if (kf == getKeyframeSelected()) {
			int size = keyframes.size();
			if (index < size) {
				setKeyframeSelected(keyframes.get(index));
			} else if (size > 0) {
				while (index > 0 && index >= size) {
					index--;
				}
				setKeyframeSelected(keyframes.get(index));
			} else {
				kf.setSelected(false);
			}
		}
	}

	public void movePrevKeyframe(Keyframe kf) {
		int index;
		Keyframes keyframes = getKeyframes();
		if ((index = keyframes.indexOf(kf)) < 1) {
			return;
		}
		keyframes.get(index - 1).setFrameNumber(index);
		keyframes.remove(index);
		keyframes.add(index - 1, kf);
		keyframeView.updateSize();
		scrollPane.getViewport().revalidate();
		repaint();
	}

	public void moveNextKeyframe(Keyframe kf) {
		int index;
		Keyframes keyframes = getKeyframes();
		if ((index = keyframes.indexOf(kf)) >= keyframes.size() - 1) {
			return;
		}
		keyframes.get(index + 1).setFrameNumber(index);
		keyframes.remove(index);
		keyframes.add(index + 1, kf);
		keyframeView.updateSize();
		scrollPane.getViewport().revalidate();
		repaint();
	}

	public boolean isOnionSkinPrev() {
		return onionSkinPrev;
	}

	public void setOnionSkinPrev(boolean onionSkinPrev) {
		this.onionSkinPrev = onionSkinPrev;
		notifyTimelineChanged(Listener.ONIONSKIN);
	}

	public boolean isOnionSkinNext() {
		return onionSkinNext;
	}

	public void setOnionSkinNext(boolean onionSkinNext) {
		this.onionSkinNext = onionSkinNext;
		notifyTimelineChanged(Listener.ONIONSKIN);
	}
}
