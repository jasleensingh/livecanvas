package livecanvas.animator;

import java.util.Arrays;
import java.util.List;

import livecanvas.Face;
import livecanvas.Matrix;
import livecanvas.Mesh;
import livecanvas.Utils;
import livecanvas.Vec2;

import umfpack.Umfpack;

public class RigidTransform {
	private static Umfpack umfpack;
	private static int matrix_id;
	private Mesh mesh;
	private double[][] E;
	private double[][] invG_C;

	private List<Vertex> input_vertices;
	private List<Vertex> output_vertices;

	public RigidTransform(Mesh mesh) {
		this.mesh = mesh;
		umfpack = null;
	}

	public void initErrorMatrix() {
		int n = mesh.vertices.length;
		E = new double[n * 2][n * 2];
		for (int i = 0; i < mesh.faces.length; i++) {
			Face face = mesh.faces[i];
			Vertex v0 = mesh.vertices[face.v1Index];
			Vertex v1 = mesh.vertices[face.v2Index];
			Vertex v2 = mesh.vertices[face.v3Index];
			int v0x = v0.index * 2;
			int v0y = v0.index * 2 + 1;
			int v1x = v1.index * 2;
			int v1y = v1.index * 2 + 1;
			int v2x = v2.index * 2;
			int v2y = v2.index * 2 + 1;
			double A02[][] = new double[2][2];
			double A12[][] = new double[2][2];
			double A10[][] = new double[2][2];
			double A20[][] = new double[2][2];
			double A21[][] = new double[2][2];
			double A01[][] = new double[2][2];
			setRelativeCoords(v0, v1, v2, A02, A12);
			setRelativeCoords(v1, v2, v0, A10, A20);
			setRelativeCoords(v2, v0, v1, A21, A01);
			setErrorMatrix(v0x, v0y, v1x, v1y, v2x, v2y, A01, A10, A12, A21,
					A20, A02, 1);
			setErrorMatrix(v1x, v1y, v2x, v2y, v0x, v0y, A12, A21, A20, A02,
					A01, A10, 1);
			setErrorMatrix(v2x, v2y, v0x, v0y, v1x, v1y, A20, A02, A01, A10,
					A12, A21, 1);
		}
	}

	static void setRelativeCoords(Vertex v0, Vertex v1, Vertex v2,
			double A02[][], double A12[][]) {
		Vec2 v01 = new Vec2(new Vec2(v0.x, v0.y), new Vec2(v1.x, v1.y));
		Vec2 v02 = new Vec2(new Vec2(v0.x, v0.y), new Vec2(v2.x, v2.y));
		double det = v01.x * v01.x + v01.y * v01.y;
		double w[] = { (v01.x * v02.x + v01.y * v02.y) / det,
				(v01.y * v02.x - v01.x * v02.y) / det };
		A02[0][0] = 1.0D - w[0];
		A02[1][0] = -w[1];
		A02[0][1] = w[1];
		A02[1][1] = 1.0D - w[0];
		A12[0][0] = w[0];
		A12[1][0] = w[1];
		A12[0][1] = -w[1];
		A12[1][1] = w[0];
	}

	private void setErrorMatrix(int v0x, int v0y, int v1x, int v1y, int v2x,
			int v2y, double A01[][], double A10[][], double A12[][],
			double A21[][], double A20[][], double A02[][], double weight) {
		E[v2x][v0x] += (-A02[0][0] + A21[0][0] * A01[0][0] + A21[0][1]
				* A01[0][1])
				- A20[0][0];
		E[v2x][v0y] += (-A02[1][0] + A21[0][0] * A01[1][0] + A21[0][1]
				* A01[1][1])
				- A20[0][1];
		E[v2x][v1x] += (-A12[0][0] - A21[0][0]) + A20[0][0] * A10[0][0]
				+ A20[0][1] * A10[0][1];
		E[v2x][v1y] += (-A12[1][0] - A21[0][1]) + A20[0][0] * A10[1][0]
				+ A20[0][1] * A10[1][1];
		E[v2x][v2x] += 1.0D + A21[0][0] * A21[0][0] + A21[0][1] * A21[0][1]
				+ A20[0][0] * A20[0][0] + A20[0][1] * A20[0][1];
		E[v2x][v2y] += A21[0][0] * A21[1][0] + A21[0][1] * A21[1][1]
				+ A20[0][0] * A20[1][0] + A20[0][1] * A20[1][1];
		E[v2y][v0x] += ((-A02[0][1] + A21[1][0] * A01[0][0] + A21[1][1]
				* A01[0][1]) - A20[1][0])
				* weight;
		E[v2y][v0y] += ((-A02[1][1] + A21[1][0] * A01[1][0] + A21[1][1]
				* A01[1][1]) - A20[1][1])
				* weight;
		E[v2y][v1x] += ((-A12[0][1] - A21[1][0]) + A20[1][0] * A10[0][0] + A20[1][1]
				* A10[0][1])
				* weight;
		E[v2y][v1y] += ((-A12[1][1] - A21[1][1]) + A20[1][0] * A10[1][0] + A20[1][1]
				* A10[1][1])
				* weight;
		E[v2y][v2x] += (A21[1][0] * A21[0][0] + A21[1][1] * A21[0][1]
				+ A20[1][0] * A20[0][0] + A20[1][1] * A20[0][1])
				* weight;
		E[v2y][v2y] += (1.0D + A21[1][0] * A21[1][0] + A21[1][1] * A21[1][1]
				+ A20[1][0] * A20[1][0] + A20[1][1] * A20[1][1])
				* weight;
	}

	public void compile(List<Vertex> input_vertices) {
		this.input_vertices = input_vertices;
		output_vertices = Utils.subtract(Arrays.asList(mesh.vertices),
				input_vertices);
		int n_output = output_vertices.size();
		int n_input = input_vertices.size();
		SparseMatrix matrix = new SparseMatrix(n_output * 2);
		for (int i = 0; i < n_output; i++) {
			Vertex u = output_vertices.get(i);
			for (int j = 0; j < n_output; j++) {
				Vertex v = output_vertices.get(j);
				if (!FloatCmp.zero(E[u.index * 2][v.index * 2])) {
					matrix.add(i * 2, j * 2, E[u.index * 2][v.index * 2]);
					matrix.add(i * 2 + 1, j * 2,
							E[u.index * 2 + 1][v.index * 2]);
					matrix.add(i * 2, j * 2 + 1,
							E[u.index * 2][v.index * 2 + 1]);
					matrix.add(i * 2 + 1, j * 2 + 1,
							E[u.index * 2 + 1][v.index * 2 + 1]);
				}
			}
		}
		Object A[] = matrix.get_umfpack_matrix();
		int Ap[] = (int[]) A[0];
		int Ai[] = (int[]) A[1];
		double Ax[] = (double[]) A[2];
		umfpack_factorize(n_output * 2, Ap, Ai, Ax);
		double C[][] = new double[output_vertices.size() * 2][input_vertices
				.size() * 2];
		for (int i = 0; i < n_output; i++) {
			Vertex u = output_vertices.get(i);
			for (int j = 0; j < n_input; j++) {
				Vertex v = input_vertices.get(j);
				if (E[u.index * 2][v.index * 2] != 0.0D) {
					C[i * 2][j * 2] -= E[u.index * 2][v.index * 2];
					C[i * 2][j * 2 + 1] -= E[u.index * 2][v.index * 2 + 1];
					C[i * 2 + 1][j * 2] -= E[u.index * 2 + 1][v.index * 2];
					C[i * 2 + 1][j * 2 + 1] -= E[u.index * 2 + 1][v.index * 2 + 1];
				}
			}
		}
		double[][] invG_C_transpose = new double[input_vertices.size() * 2][output_vertices
				.size() * 2];
		double q[] = new double[input_vertices.size() * 2];
		for (int i = 0; i < input_vertices.size() * 2; i++) {
			q[i] = 1.0D;
			double[] B = Matrix.multiply(C, q);
			invG_C_transpose[i] = umfpack_solve(B);
			q[i] = 0.0D;
		}
		invG_C = Matrix.transpose(invG_C_transpose);
	}

	private static double[] umfpack_solve(double B[]) {
		double x[] = new double[B.length];
		umfpack.solve(matrix_id, B, x);
		return x;
	}

	private static void umfpack_factorize(int n, int Ap[], int Ai[],
			double Ax[]) {
		if (umfpack == null)
			umfpack = new Umfpack();
		else
			umfpack.release(matrix_id);
		matrix_id = umfpack.factorize(n, Ap, Ai, Ax);
	}

	public void update() {
		int n_input = input_vertices.size();
		int n_output = output_vertices.size();
		double q[] = new double[n_input * 2];
		for (int i = 0; i < n_input; i++) {
			Vertex v = input_vertices.get(i);
			q[i * 2] = v.x;
			q[i * 2 + 1] = v.y;
		}
		double output[] = Matrix.multiply(invG_C, q);
		for (int i = 0; i < n_output; i++) {
			Vertex v = output_vertices.get(i);
			v.x = output[i * 2];
			v.y = output[i * 2 + 1];
		}
	}
}
