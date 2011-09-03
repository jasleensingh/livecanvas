package livecanvas.image;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import common.typeutils.AutoPanel;
import common.typeutils.Property;
import common.typeutils.PropertyFactory;

public class Style<T> {
	public final String name;
	public final ParticleGenerator<T> generator;
	public final Renderer<T> renderer;

	public Style(String name, ParticleGenerator<T> generator,
			Renderer<T> renderer) {
		this.name = name;
		this.generator = generator;
		this.renderer = renderer;
	}

	public void showSettings(Component parent) {
		Property[] generatorProps = PropertyFactory.createProperties(generator);
		Property[] rendererProps = PropertyFactory.createProperties(renderer);
		if (generatorProps.length == 0 && rendererProps.length == 0) {
			return;
		}
		JPanel settingsPnl = new JPanel(new BorderLayout());
		// settingsPnl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JTabbedPane tabs = new JTabbedPane();
		if (generatorProps.length != 0) {
			tabs.addTab("Generator", new AutoPanel(generatorProps));
		}
		if (rendererProps.length != 0) {
			tabs.addTab("Renderer", new AutoPanel(rendererProps));
		}
		settingsPnl.add(tabs);
		JOptionPane.showMessageDialog(parent, settingsPnl, "Style Properties",
				JOptionPane.PLAIN_MESSAGE);
	}

	@Override
	public String toString() {
		return name;
	}

	public static final Style[] Styles;
	static {
		List<Style> styles = new LinkedList<Style>();
		styles.add(new Style("Dots", new CentroidParticleGenerator(),
				new DotRenderer()));
		styles.add(new Style("Faces", new CentroidParticleGenerator(),
				new FaceRenderer()));
		styles.add(new Style("Mesh", new MeshParticleGenerator(),
				new MeshRenderer()));
		styles.add(new Style("Dithered", new DitheredParticleGenerator(),
				new DitheredRenderer()));
		styles.add(new Style("Halftone", new HalftoneParticleGenerator(),
				new HalftoneRenderer()));
		styles.add(new Style("Sandman", new SandmanParticleGenerator(),
				new SandmanRenderer()));
		styles.add(new Style("Line Strokes", new LineStrokeParticleGenerator(),
				new LineStrokeRenderer()));
		styles.add(new Style("Texture Strokes",
				new TextureStrokeParticleGenerator(),
				new TextureStrokeRenderer()));
		styles.add(new Style("Painterly", new PainterlyParticleGenerator(),
				new PainterlyRenderer()));
		styles.add(new Style("Mosaic", new MosaicParticleGenerator(),
				new MosaicRenderer()));
		Styles = styles.toArray(new Style[0]);
	}
}
