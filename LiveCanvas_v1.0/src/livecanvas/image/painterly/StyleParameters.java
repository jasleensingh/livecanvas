package livecanvas.image.painterly;

import common.typeutils.DoubleType;
import common.typeutils.IntegerType;

public class StyleParameters {

	@DoubleType(name = "Approximation Threshold", min = 0.0, max = 1.0, step = 0.001)
	public double approxThreshold = 0.01;

	@DoubleType(name = "Min Brush Size")
	public double minBrushSize = 4;

	@DoubleType(name = "Brush Size Ratio")
	public double brushSizeRatio = 2;

	@IntegerType(name = "Num Brushes")
	public int nBrushes = 3;

	@DoubleType(name = "Curvature Filter", min = 0, max = 1, step = 0.1)
	public double curvatureFilter = 1.0;

	@DoubleType(name = "Blur Factor", min = 0, max = 10, step = 0.1)
	public double blurFactor = .2;

	@IntegerType(name = "Min Stroke Length", min = 1, max = 100)
	public int minStrokeLength = 4;

	@IntegerType(name = "Max Stroke Length", min = 1, max = 100)
	public int maxStrokeLength = 16;

	@DoubleType(name = "Opacity", min = 0.1, max = 1, step = 0.05)
	public double opacity = .5;

	@DoubleType(name = "Grid Size", min = 0.1, max = 10, step = 0.1)
	public double gridSize = .5;

	@DoubleType(name = "Color Jitter", min = 0, max = 1, step = 0.1)
	public double colorJitter = 0.5;

	public void copy(StyleParameters styleParameters) {
		this.approxThreshold = styleParameters.approxThreshold;
		this.minBrushSize = styleParameters.minBrushSize;
		this.brushSizeRatio = styleParameters.brushSizeRatio;
		this.nBrushes = styleParameters.nBrushes;
		this.curvatureFilter = styleParameters.curvatureFilter;
		this.blurFactor = styleParameters.blurFactor;
		this.minStrokeLength = styleParameters.minStrokeLength;
		this.maxStrokeLength = styleParameters.maxStrokeLength;
		this.opacity = styleParameters.opacity;
		this.gridSize = styleParameters.gridSize;
		this.colorJitter = styleParameters.colorJitter;
	}
}
