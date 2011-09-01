package livecanvas;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.LinkedList;
import java.util.List;

import livecanvas.Tool.Pointer.PointerHandler;

public class CanvasDraw extends Canvas {
	protected List<Cel> cels = new LinkedList<Cel>();
	protected Cel currCel;

	private PointerHandler pointerHandler;

	public CanvasDraw(int width, int height, PointerHandler pointerHandler) {
		super(width, height);
		this.pointerHandler = pointerHandler;
		currCel = new Cel(0, width, height);
		addCel(currCel);
	}

	protected void addTools() {
		toolsMap.put(TOOLS_PENCIL, new Tool.Pencil(this));
		toolsMap.put(TOOLS_BRUSH, new Tool.Brush(this));
		toolsMap.put(TOOLS_PEN, new Tool.Pen(this));
		toolsMap.put(TOOLS_ERASE, new Tool.Erase(this));
		toolsMap.put(TOOLS_SELECT, new Tool.Select(this));
		toolsMap.put(TOOLS_POINTER, new Tool.Pointer(this, pointerHandler));
		toolsMap.put(TOOLS_PANZOOM, new Tool.PanZoom(this));
	}

	@Override
	public void setSize(int width, int height) {
		for (Cel cel : cels) {
			cel.setSize(width, height);
		}
		super.setSize(width, height);
	}

	public void addCel(Cel cel) {
		cels.add(cel);
	}

	public void removeCel(Cel cel) {
		cels.remove(cel);
	}

	public void removeAllCels() {
		cels.clear();
	}

	public void createCelsUpto(int index) {
		for (int i = cels.size(); i <= index; i++) {
			addCel(new Cel(i, 800, 600));
		}
	}

	public Cel getCurrCel() {
		return currCel;
	}

	public void setCurrCel(int index) {
		currCel = getCel(index);
		repaint();
	}

	public void setCurrCel(Cel currCel) {
		this.currCel = currCel;
	}

	public Cel getCel(int index) {
		return cels.size() > index ? cels.get(index) : null;
	}

	public Cel[] getCels() {
		return cels.toArray(new Cel[0]);
	}

	public int getCelCount() {
		return cels.size();
	}

	private static AlphaComposite sLighten = AlphaComposite.getInstance(
			AlphaComposite.SRC_OVER, 0.25f);

	private boolean onion = true;

	@Override
	public void paint(Graphics2D g, int width, int height) {
		if (currCel.backgroundRef != null) {
			Composite c = g.getComposite();
			g.setComposite(sLighten);
			g.drawImage(currCel.backgroundRef, -width / 2, -height / 2, null);
			g.setComposite(c);
		}
		if (currCel.index > 0 && onion) {
			Composite c = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					0.1f));
			g.drawImage(cels.get(currCel.index - 1).buf, -width / 2,
					-height / 2, width, height, null);
			g.setComposite(c);
		}
		g.drawImage(currCel.buf, -width / 2, -height / 2, width, height, null);
		super.paint(g, width, height);
	}

	public void pathCreated(Point[] path) {
	}

	public void requestImagePaint(Tool t) {
		Graphics2D g = currCel.buf.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.translate(getWidth() / 2, getHeight() / 2);
		t.imagePaint(g, getWidth(), getHeight());
		repaint();
	}
}
