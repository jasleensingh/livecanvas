package livecanvas.animator;

import livecanvas.components.Keyframe;

public interface KeyframesContainer {
	public Keyframes getKeyframes();

	public Keyframe getSelectedKeyframe();

	public void updateMeshFromKeyframe(Keyframe kf);

	public void updateMeshFromKeyframes(Keyframe kf1, Keyframe kf2,
			float interpolation);
}
