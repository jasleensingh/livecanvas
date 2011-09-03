package livecanvas.sandman;

import javax.swing.JApplet;
import javax.swing.UIManager;

public class Applet extends JApplet {

	public void init() {
		initLnf();
	}

	public void start() {
		initLnf();
		setContentPane(new Sandman());
	}

	private void initLnf() {
		UIManager.put("ClassLoader", Applet.class.getClassLoader());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
