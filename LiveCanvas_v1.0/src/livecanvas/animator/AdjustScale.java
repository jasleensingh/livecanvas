package livecanvas.animator;

import java.util.Arrays;
import java.util.List;

import livecanvas.Face;
import livecanvas.Mesh;
import livecanvas.Utils;
import livecanvas.Vec2;
import umfpack.Umfpack;
import VisualNumerics.math.DoubleMatrix;

public class AdjustScale {
	private Umfpack umfpack;
	private int matrix_id;
	private Mesh mesh;
	private double[][][] A02s;
	private double[][][] A12s;
	private double[][][] M_inverse;
	private double[] original_length01;
	private double[][] Mx;
	private double[] Bx;
	private double[] By;
	private List<Vertex> input_vertices;
	private List<Vertex> output_vertices;

	public AdjustScale(Mesh mesh) {
		this.mesh = mesh;
		umfpack = null;
		computeFitUnscaledMatrix();
		computeConnectivityMatrix();
	}

	private void computeFitUnscaledMatrix() {
		M_inverse = new double[mesh.faces.length][][];
		A02s = new double[mesh.faces.length][][];
		A12s = new double[mesh.faces.length][][];
		original_length01 = new double[mesh.faces.length];
		for (Face f : mesh.faces) {
			computeFitUnscaledMatrix(f);
		}
	}

	// Matrix F in the paper (section 4.2.1)
	private void computeFitUnscaledMatrix(Face face) {
		int i = face.index;
		Vertex v0 = mesh.vertices[face.v1Index];
		Vertex v1 = mesh.vertices[face.v2Index];
		Vertex v2 = mesh.vertices[face.v3Index];
		double A02[][] = new double[2][2];
		double A12[][] = new double[2][2];
		RigidTransform.setRelativeCoords(v0, v1, v2, A02, A12);
		A02 = DoubleMatrix.transpose(A02);
		A12 = DoubleMatrix.transpose(A12);
		double M[][] = {
				{ 1.0D + A02[0][0] * A02[0][0] + A02[1][0] * A02[1][0],
						A02[0][0] * A02[0][1] + A02[1][0] * A02[1][1],
						A02[0][0] * A12[0][0] + A02[1][0] * A12[1][0],
						A02[0][0] * A12[0][1] + A02[1][0] * A12[1][1] },
				{ A02[0][1] * A02[0][0] + A02[1][1] * A02[1][0],
						1.0D + A02[0][1] * A02[0][1] + A02[1][1] * A02[1][1],
						A02[0][1] * A12[0][0] + A02[1][1] * A12[1][0],
						A02[0][1] * A12[0][1] + A02[1][1] * A12[1][1] },
				{ A12[0][0] * A02[0][0] + A12[1][0] * A02[1][0],
						A12[0][0] * A02[0][1] + A12[1][0] * A02[1][1],
						1.0D + A12[0][0] * A12[0][0] + A12[1][0] * A12[1][0],
						A12[0][0] * A12[0][1] + A12[1][0] * A12[1][1] },
				{ A12[0][1] * A02[0][0] + A12[1][1] * A02[1][0],
						A12[0][1] * A02[0][1] + A12[1][1] * A02[1][1],
						A12[0][1] * A12[0][0] + A12[1][1] * A12[1][0],
						1.0D + A12[0][1] * A12[0][1] + A12[1][1] * A12[1][1] } };
		try {
			M_inverse[i] = DoubleMatrix.inverse(M);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error inverting matrix!");
		}
		A02s[i] = A02;
		A12s[i] = A12;
		original_length01[i] = new Vec2(new Vec2(v0.x, v0.y), new Vec2(v1.x,
				v1.y)).length();
	}

	// Matrix H in the paper (section 4.2.2)
	private void computeConnectivityMatrix() {
		int n = mesh.vertices.length;
		Mx = new double[n][n];
		for (int i = 0; i < mesh.faces.length; i++) {
			Face face = mesh.faces[i];
			Vertex v0 = mesh.vertices[face.v1Index];
			Vertex v1 = mesh.vertices[face.v2Index];
			Vertex v2 = mesh.vertices[face.v3Index];
			setConnectivityMatrixSub(v0, v1, v2, face.weight);
			setConnectivityMatrixSub(v1, v2, v0, face.weight);
			setConnectivityMatrixSub(v2, v0, v1, face.weight);
		}
	}

	private void setConnectivityMatrixSub(Vertex v0, Vertex v1, Vertex v2,
			double weight) {
		Mx[v0.index][v0.index] += weight;
		Mx[v0.index][v1.index] += -weight;
		Mx[v0.index][v0.index] += weight;
		Mx[v0.index][v2.index] += -weight;
	}

	public void compile(List<Vertex> input_vertices) {
		this.input_vertices = input_vertices;
		output_vertices = Utils.subtract(Arrays.asList(mesh.vertices),
				input_vertices);
		int n_output = output_vertices.size();
		SparseMatrix matrix = new SparseMatrix(n_output);
		for (int i = 0; i < n_output; i++) {
			Vertex u = output_vertices.get(i);
			for (int j = 0; j < n_output; j++) {
				Vertex v = output_vertices.get(j);
				matrix.add(i, j, Mx[u.index][v.index]);
			}
		}
		Object A[] = matrix.get_umfpack_matrix();
		int Ap[] = (int[]) A[0];
		int Ai[] = (int[]) A[1];
		double Ax[] = (double[]) A[2];
		umfpack_factorize(n_output, Ap, Ai, Ax);
	}

	private double[] umfpack_solve(double B[]) {
		double x[] = new double[B.length];
		umfpack.solve(matrix_id, B, x);
		return x;
	}

	private void umfpack_factorize(int n, int Ap[], int Ai[], double Ax[]) {
		if (umfpack == null)
			umfpack = new Umfpack();
		else
			umfpack.release(matrix_id);
		matrix_id = umfpack.factorize(n, Ap, Ai, Ax);
	}

	private void computeRHS() {
		int n = mesh.vertices.length;
		Bx = new double[n];
		By = new double[n];
		for (Face f : mesh.faces) {
			Vec2[] fittedFace = computeFittedFace(f);
			Vec2 d01 = fittedFace[0];
			Vec2 d12 = fittedFace[1];
			Vec2 d20 = fittedFace[2];
			Vertex v0 = mesh.vertices[f.v1Index];
			Vertex v1 = mesh.vertices[f.v2Index];
			Vertex v2 = mesh.vertices[f.v3Index];
			setRHS(v0, v1, v2, d01, d12, d20, f.weight);
			setRHS(v1, v2, v0, d12, d20, d01, f.weight);
			setRHS(v2, v0, v1, d20, d01, d12, f.weight);
		}
	}

	private Vec2[] computeFittedFace(Face f) {
		Vertex v0 = mesh.vertices[f.v1Index];
		Vertex v1 = mesh.vertices[f.v2Index];
		Vertex v2 = mesh.vertices[f.v3Index];
		double[][] A02 = A02s[f.index];
		double[][] A12 = A12s[f.index];
		double b[] = { v0.x + A02[0][0] * v2.x + A02[1][0] * v2.y,
				v0.y + A02[0][1] * v2.x + A02[1][1] * v2.y,
				v1.x + A12[0][0] * v2.x + A12[1][0] * v2.y,
				v1.y + A12[0][1] * v2.x + A12[1][1] * v2.y };
		double x[] = DoubleMatrix.multiply(M_inverse[f.index], b);
		Vec2 u0 = new Vec2(x[0], x[1]);
		Vec2 u1 = new Vec2(x[2], x[3]);
		Vec2 u2 = new Vec2(A02[0][0] * u0.x + A02[0][1] * u0.y + A12[0][0]
				* u1.x + A12[0][1] * u1.y, A02[1][0] * u0.x + A02[1][1] * u0.y
				+ A12[1][0] * u1.x + A12[1][1] * u1.y);
		Vec2 d01 = new Vec2(u0, u1);
		Vec2 d12 = new Vec2(u1, u2);
		Vec2 d20 = new Vec2(u2, u0);
		double ratio = original_length01[f.index] / d01.length();
		d01.multiplySelf(ratio);
		d12.multiplySelf(ratio);
		d20.multiplySelf(ratio);
		// add_fitted_triangle(face, d01, d12, d20);
		Vec2[] result = { d01, d12, d20 };
		return result;
	}

	private void setRHS(Vertex v0, Vertex v1, Vertex v2, Vec2 d01, Vec2 d12,
			Vec2 d20, double weight) {
		Bx[v0.index] += (-d01.x + d20.x) * weight;
		By[v0.index] += (-d01.y + d20.y) * weight;
	}

	public void update() {
		computeRHS();
		int n_input = input_vertices.size();
		int n_output = output_vertices.size();
		double bx[] = new double[n_output];
		double by[] = new double[n_output];
		double vx[];
		double vy[];
		for (int i = 0; i < n_output; i++) {
			Vertex u = output_vertices.get(i);
			bx[i] = Bx[u.index];
			by[i] = By[u.index];
			for (int j = 0; j < n_input; j++) {
				Vertex v = input_vertices.get(j);
				bx[i] += -Mx[u.index][v.index] * v.x;
				by[i] += -Mx[u.index][v.index] * v.y;
			}
		}
		vx = umfpack_solve(bx);
		vy = umfpack_solve(by);
		for (int i = 0; i < n_output; i++) {
			Vertex v = output_vertices.get(i);
			v.x = vx[i];
			v.y = vy[i];
		}
	}
}
