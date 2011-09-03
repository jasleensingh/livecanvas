package livecanvas;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class Progress {
	public static int DONE = 0, NOT_DONE = 1, CANCELED = -1;

	public static interface Indicator {
		public void setProgress(String text, double progress);

		public boolean isCanceled();

		public void error(String message);

		public static final Indicator CONSOLE = new Indicator() {
			@Override
			public void setProgress(String text, double progress) {
				System.out.println(String.format("%s [%d%%]", text,
						(int) (progress * 100)));
			}

			@Override
			public boolean isCanceled() {
				return false;
			}

			@Override
			public void error(String message) {
				System.err.println(message);
			}
		};
	}

	public static interface Task {
		public String description();

		public void run(Progress.Indicator progress);
	}

	public static class Dialog extends JDialog {
		private volatile boolean canceled;
		private Indicator progress;
		private JLabel label;
		private JProgressBar progressBar;
		private Task task;

		private Dialog(Component parent, Task task) {
			super(JOptionPane.getFrameForComponent(parent), "Please wait - "
					+ task.description(), true);
			this.task = task;
			JPanel content = new JPanel(new BorderLayout(10, 10));
			content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			JPanel center = new JPanel(new BorderLayout(10, 10));
			label = new JLabel();
			center.add(label, BorderLayout.NORTH);
			progressBar = new JProgressBar(0, 100);
			center.add(progressBar, BorderLayout.SOUTH);
			center.setPreferredSize(new Dimension(300, 100));
			content.add(center);
			JPanel east = new JPanel(new BorderLayout());
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					canceled = true;
					close();
				}
			});
			east.add(cancel, BorderLayout.NORTH);
			content.add(east, BorderLayout.EAST);
			setContentPane(content);
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					canceled = true;
					close();
				}
			});
			pack();
			setLocationRelativeTo(getParent());
			setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		}

		private void close() {
			setVisible(false);
			dispose();
		}

		private void start() {
			progress = new Indicator() {
				@Override
				public void setProgress(String text, double progress) {
					label.setText(text);
					progressBar.setValue((int) (Math.max(0,
							Math.min(1, progress)) * progressBar.getMaximum()));
					setTitle("Please wait - " + task.description());
				}

				@Override
				public boolean isCanceled() {
					return canceled;
				}

				@Override
				public void error(String message) {
					close();
					JOptionPane.showMessageDialog(getParent(), message,
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			};
			new Thread() {
				public void run() {
					task.run(progress);
					if (isVisible()) {
						close();
					}
				}
			}.start();
			setVisible(true);
		}

		public static void show(Component parent, Task task) {
			new Dialog(parent, task).start();
		}
	}
}
