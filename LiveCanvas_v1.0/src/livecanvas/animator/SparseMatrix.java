package livecanvas.animator;

import java.util.LinkedList;
import java.util.List;

public class SparseMatrix {
	private static class Entry {
		public final int row;
		public double value;

		Entry(int row, double value) {
			this.row = row;
			this.value = value;
		}
	}

	/**
	 * Number of columns
	 */
	private int n;
	private List<Entry>[] columns;

	public SparseMatrix(int n) {
		this.n = n;
		columns = new List[n];
		for (int i = 0; i < n; i++)
			columns[i] = new LinkedList<Entry>();

	}

	public void add(int row, int col, double value) {
		if (value == 0.0D)
			return;
		List<Entry> column = columns[col];
		int i = 0;
		for (i = 0; i < column.size(); i++) {
			Entry entry = column.get(i);
			if (row < entry.row) {
				column.add(i, new Entry(row, value));
				break;
			}
			if (row != entry.row)
				continue;
			entry.value += value;
			break;
		}
		if (i >= column.size()) {
			column.add(new Entry(row, value));
		}
	}

	public Object[] get_umfpack_matrix() {
		int n_of_entries = 0;
		for (int i = 0; i < n; i++)
			n_of_entries += columns[i].size();
		int Ap[] = new int[n + 1]; // column boundaries
		int Ai[] = new int[n_of_entries]; // row indices
		double Ax[] = new double[n_of_entries]; // values
		int index = 0;
		for (int i = 0; i < n; i++) {
			Ap[i] = index;
			List<Entry> column = columns[i];
			for (int j = 0; j < column.size(); j++) {
				Entry entry = column.get(j);
				Ai[index] = entry.row;
				Ax[index] = entry.value;
				index++;
			}
		}
		Ap[n] = index;
		Object result[] = { Ap, Ai, Ax };
		return result;
	}
}
