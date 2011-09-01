package livecanvas;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

public abstract class View extends JPanel implements Constants, ActionListener {

	private List<View> chain = new LinkedList<View>();

	public View() {
	}

	public View(LayoutManager mgr) {
		super(mgr);
	}

	public void chain(View v) {
		chain.add(v);
	}

	public abstract boolean handleEvent(ActionEvent e);

	@Override
	public final void actionPerformed(ActionEvent e) {
		for (View v : chain) {
			if (v.handleEvent(e)) {
				return;
			}
		}
		handleEvent(e);
	}
}
