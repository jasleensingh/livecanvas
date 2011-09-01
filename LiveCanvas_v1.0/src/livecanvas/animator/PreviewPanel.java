package livecanvas.animator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import livecanvas.CanvasMesh;
import livecanvas.Constants;
import livecanvas.Tool.Pointer.PointerHandler;
import livecanvas.Utils;
import livecanvas.Utils.ButtonType;

public class PreviewPanel extends JPanel implements Constants, ActionListener {
	private static final Class clazz = PreviewPanel.class;

	private CanvasMesh canvas;
	private BufferedImage[] renderedFrames;
	private int currKeyframe = 0;

	public PreviewPanel(CanvasMesh source, BufferedImage[] renderedFrames) {
		super(new BorderLayout());
		System.err.println(renderedFrames.length + " frames in preview");
		this.renderedFrames = renderedFrames;
		JPanel north = new JPanel(new BorderLayout());
		north.add(new JSeparator(), BorderLayout.SOUTH);
		north.add(createToolBar());
		add(north, BorderLayout.NORTH);

		JPanel imageRender = new JPanel() {
			protected void paintComponent(Graphics g) {
				if (PreviewPanel.this.renderedFrames == null) {
					return;
				}
				g.drawImage(PreviewPanel.this.renderedFrames[currKeyframe], 0,
						0, null);
			}
		};
		imageRender.setPreferredSize(new Dimension(source.getWidth(), source
				.getHeight()));
		add(imageRender);

		canvas = new CanvasMesh(source.getWidth(), source.getHeight(),
				PointerHandler.NULL);
		canvas.setCurrLayer(source.getCurrLayer());
		canvas.initializeTransform();
	}

	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
		JButton toolsButton;
		toolbar.add(toolsButton = Utils.createToolBarButton("Start",
				new ImageIcon(clazz.getResource("res/start.png")),
				ButtonType.ICON_ONLY, PREVIEW_START, "Start", this));
		toolbar.add(toolsButton = Utils.createToolBarButton("Pause",
				new ImageIcon(clazz.getResource("res/pause.png")),
				ButtonType.ICON_ONLY, PREVIEW_PAUSE, "Pause", this));
		toolbar.add(toolsButton = Utils.createToolBarButton("Stop",
				new ImageIcon(clazz.getResource("res/stop.png")),
				ButtonType.ICON_ONLY, PREVIEW_STOP, "Stop", this));
		toolbar.add(toolsButton = Utils.createToolBarButton("PreviousFrame",
				new ImageIcon(clazz.getResource("res/prev_frame.png")),
				ButtonType.ICON_ONLY, PREVIEW_PREV, "Previous", this));
		toolbar.add(toolsButton = Utils.createToolBarButton("NextFrame",
				new ImageIcon(clazz.getResource("res/next_frame.png")),
				ButtonType.ICON_ONLY, PREVIEW_NEXT, "Next Frame", this));
		// toolsButton.setSelected(true);
		return toolbar;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (Integer.parseInt(e.getActionCommand())) {
		case PREVIEW_START:
			start();
			break;
		case PREVIEW_STOP:
			stop();
			break;
		case PREVIEW_PAUSE:
			pause();
			break;
		case PREVIEW_PREV:
			prev();
			break;
		case PREVIEW_NEXT:
			next();
			break;
		}
		repaint();
	}

	private Timer timer;

	public void start() {
		if (renderedFrames == null) {
			return;
		}
		if (timer != null) {
			return;
		}
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				currKeyframe = (currKeyframe + 1) % renderedFrames.length;
				repaint();
			}
		}, 0, 100);
	}

	public void stop() {
		pause();
		currKeyframe = 0;
	}

	public void pause() {
		if (timer == null) {
			return;
		}
		timer.cancel();
		timer = null;
	}

	public void next() {
		if (timer != null) {
			pause();
		}
		currKeyframe = (currKeyframe + 1) % renderedFrames.length;
	}

	public void prev() {
		if (timer != null) {
			pause();
		}
		currKeyframe = (currKeyframe - 1 + renderedFrames.length)
				% renderedFrames.length;
	}
}
