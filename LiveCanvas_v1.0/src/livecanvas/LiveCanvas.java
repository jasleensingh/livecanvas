package livecanvas;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import livecanvas.Utils.ButtonType;
import livecanvas.animator.Animator;
import livecanvas.mesheditor.MeshEditor;
import livecanvas.sketchcreator.SketchCreator;

public class LiveCanvas extends JFrame {
	private List<Perspective> perspectives;

	private ActionListener appActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			setSelectedPerspective(perspectives.get(Integer.parseInt(e
					.getActionCommand())));
		}
	};
	private JPanel tabsPanel;
	private JPanel tabsContainer;
	private JPanel toolBarContainer;

	public LiveCanvas() {
		super("Live Canvas");
		perspectives = new LinkedList<Perspective>();
		perspectives.add(new SketchCreator());
		perspectives.add(new MeshEditor());
		perspectives.add(new Animator());
		tabsPanel = new JPanel(new BorderLayout());
		tabsPanel.setPreferredSize(new Dimension(1200, 720));
		JPanel north = new JPanel(new BorderLayout());
		toolBarContainer = new JPanel(new BorderLayout());
		north.add(toolBarContainer);
		JToolBar tabButtons = new JToolBar();
		tabButtons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		ButtonGroup perspBg = new ButtonGroup();
		JToggleButton tabButton;
		int i = 0;
		for (Perspective p : perspectives) {
			tabButtons.add(tabButton = Utils.createToolBarToggleButton(p.name,
					p.icon, ButtonType.TEXT_AND_ICON, i++, "Open " + p.name
							+ " perspective", perspBg, appActionListener));
			if (i == 1) {
				tabButton.setSelected(true);
			}
		}
		north.add(tabButtons, BorderLayout.EAST);
		tabsPanel.add(north, BorderLayout.NORTH);

		tabsContainer = new JPanel(new BorderLayout());
		tabsPanel.add(tabsContainer);
		setSelectedPerspective(perspectives.get(0));

		setContentPane(tabsPanel);
		pack();
		setLocationRelativeTo(null);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	private void setSelectedPerspective(Perspective perspective) {
		tabsContainer.removeAll();
		tabsContainer.add(perspective);
		toolBarContainer.removeAll();
		toolBarContainer.add(perspective.getToolBar());
		setJMenuBar(perspective.getMenuBar());
		tabsPanel.revalidate();
		tabsPanel.repaint();
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new LiveCanvas().setVisible(true);
	}
}
