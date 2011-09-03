package livecanvas.image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import livecanvas.BackgroundRef;
import livecanvas.Path;
import livecanvas.Progress;
import livecanvas.components.Layer;

public abstract class AbstractParticleGenerator<T> implements
		ParticleGenerator<T> {
	protected int state;

	@Override
	public Particle<T>[] generate(RenderData data, Progress.Indicator progress) {
		List<Layer> list = new LinkedList<Layer>();
		list.add(data.layer);
		list.addAll(data.layer.getSubLayersRecursively());
		List<Particle<T>> particles = new ArrayList<Particle<T>>();
		int n = -1, nn, steps = 10;
		for (int i = 0; i < list.size(); i++) {
			if ((nn = i * steps / list.size()) != n) {
				n = nn;
				progress.setProgress(String.format("%d%% complete", nn * 10),
						(double) n / steps);
			}
			if (progress.isCanceled()) {
				state(Progress.CANCELED);
				return null;
			}
			Layer l = list.get(i);
			Path path = l.getPath();
			BackgroundRef bgref = l.findBackgroundRef();
			if (path.isFinalized() && bgref != null) {
				Particle<T>[] meshParticles = generateForPath(path, data,
						bgref);
				particles.addAll(Arrays.asList(meshParticles));
			}
		}
		state(Progress.DONE);
		return particles.toArray(new Particle[0]);
	}

	@Override
	public void pass(RenderData data) {
	}

	protected void state(int state) {
		this.state = state;
	}

	@Override
	public int state() {
		return state;
	}

	protected abstract Particle<T>[] generateForPath(Path path,
			RenderData renderData, BackgroundRef bgref);
}
