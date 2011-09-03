package livecanvas;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import livecanvas.components.Layer;
import livecanvas.components.Viewpoint;

public class AffineTransformPointerHandler extends PointerHandler implements
		Constants {
	private static final int POINTER_NONE = 0, POINTER_TRANSLATE = 1,
			POINTER_ROTATE_Z = 2, POINTER_ROTATE_Y = 3, POINTER_ROTATE_X = 4,
			POINTER_SCALE = 5;

	private int action = POINTER_NONE;
	private int lastX, lastY, currX, currY;
	private double lastAngle, currAngle, startAngle;
	private double px, py, pz;
	private CanvasMesh canvas;

	public AffineTransformPointerHandler(CanvasMesh canvas) {
		this.canvas = canvas;
	}

	public void mousePressed(MouseEvent e) {
		Layer currLayer = canvas.getCurrLayer();
		Rectangle bounds = currLayer.getPath().getBounds();
		px = bounds.getCenterX();
		py = bounds.getCenterY();
		pz = currLayer.getCenterZ();
		action = updateState(e, px, py);
		lastX = currX;
		lastY = currY;
		startAngle = lastAngle = currAngle;
	}

	public void mouseReleased(MouseEvent e) {
		action = POINTER_NONE;
	}

	public void mouseDragged(MouseEvent e) {
		updateState(e, px, py);
		switch (action) {
		case POINTER_TRANSLATE: {
			int dX = currX - lastX;
			int dY = currY - lastY;
			if (Math.abs(dX) < MOVE_STEPSIZE && Math.abs(dY) < MOVE_STEPSIZE) {
				return;
			}
			canvas.getCurrLayer().translate(dX, dY, 0);
			break;
		}
		case POINTER_ROTATE_Z: {
			double dAngle = currAngle - lastAngle;
			if (Math.abs(dAngle) < ANGLE_STEPSIZE) {
				return;
			}
			canvas.getCurrLayer().rotateZ(dAngle, px, py, pz);
			break;
		}
		case POINTER_ROTATE_Y: {
			double dAngle = currAngle - lastAngle;
			if (Math.abs(dAngle) < ANGLE_STEPSIZE) {
				return;
			}
			Layer l = canvas.getCurrLayer();
			Viewpoint vp = l.getCurrentViewpoint();
			l.setCurrViewpoint(
					(int) (Utils.angle_PItoPI(currAngle - startAngle) / ANGLE_STEPSIZE),
					vp.viewpointY);
			// canvas.getCurrLayer().rotateY(dAngle, px, py, pz);
			break;
		}
		case POINTER_ROTATE_X: {
			double dAngle = currAngle - lastAngle;
			if (Math.abs(dAngle) < ANGLE_STEPSIZE) {
				return;
			}
			Layer l = canvas.getCurrLayer();
			Viewpoint vp = l.getCurrentViewpoint();
			l.setCurrViewpoint(
					vp.viewpointX,
					(int) (Utils.angle_PItoPI(currAngle - startAngle) / ANGLE_STEPSIZE));
			// canvas.getCurrLayer().rotateY(dAngle, px, py, pz);
			break;
		}
		case POINTER_SCALE: {
			int dX = currX - lastX;
			int dY = currY - lastY;
			if (Math.abs(dX) < MOVE_STEPSIZE && Math.abs(dY) < MOVE_STEPSIZE) {
				return;
			}
			canvas.getCurrLayer().resize(dX, dY, 0);
			break;
		}
		}
		lastX = currX;
		lastY = currY;
		lastAngle = currAngle;
		canvas.repaint();
	}

	private int updateState(MouseEvent e, double cx, double cy) {
		currX = e.getX();
		currY = e.getY();
		double dx = currX - cx;
		double dy = currY - cy;
		currAngle = Math.atan2(dy, dx);
		double r = Math.sqrt(dx * dx + dy * dy);
		int action = POINTER_NONE;
		if (r < sActionRadius[sActionRadius.length - 1]) {
			if (r < sActionRadius[0]) {
				action = POINTER_TRANSLATE;
			} else if (r < sActionRadius[1]) {
				action = POINTER_ROTATE_Z;
			} else if (r < sActionRadius[2]) {
				action = POINTER_SCALE;
				// } else if (r < sActionRadius[3]) {
				// action = POINTER_ROTATE_Y;
				// } else if (r < sActionRadius[4]) {
				// action = POINTER_ROTATE_X;
			}
		}
		return action;
	}
}