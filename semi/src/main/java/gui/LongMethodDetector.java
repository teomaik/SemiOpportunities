package gui;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.macrofocus.treemap.AlgorithmFactory;
import com.macrofocus.treemap.LabelingFactory;
import com.macrofocus.treemap.RenderingFactory;
import com.macrofocus.treemap.TreeMap;

import splitlongmethod.JavaClass;

/**
 *
 * @author Antonis Gkortzis (s2583070, antonis.gkortzis@gmail.com)
 */
public class LongMethodDetector {

	private static JFileChooser fc;
	private File java_source_file;
	private Analyser analyser;
	JavaClass clazz;
	TreeMap treeMap;
	public static boolean DebugMode = true;
	private String selected_method;
	public MethodOppExtractorSettings extractor_settings;
	public static String cohesion_metric = "LCOM2";
	public static int cohesion_metric_index = 2;
	private Task task;

	public LongMethodDetector() {
		// super("Split Long Method");
		fc = new JFileChooser("./test");
		fc.setDialogTitle("Select a Java file or a folder..");
		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.extractor_settings = new MethodOppExtractorSettings();
		if (LongMethodDetector.DebugMode) {
			System.out.println("###### System Info ######\n" + utils.Utilities.getSystemSettings() + "\n");
		}
		initComponents();
//        this.settingsMenu.setVisible(false); //FIXME: uncomment before final deliverable
		this.detectLongMethodsMenuItem.setVisible(false);
		jTable1.setRowSelectionAllowed(true);

		ListSelectionModel cellSelectionModel = jTable1.getSelectionModel();
		cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					try {
						String selectedData = null;
						int[] selectedRow = jTable1.getSelectedRows();
						int[] selectedColumns = jTable1.getSelectedColumns();

						for (int i = 0; i < selectedRow.length; i++) {
							for (int j = 0; j < selectedColumns.length; j++) {
								selectedData = (String) jTable1.getValueAt(selectedRow[i], 0);
							}
						}
						selected_method = selectedData;
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});

	}

	private void initComponents() {
		
	}// </editor-fold>//GEN-END:initComponents

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		if (args.length > 0) {
			if (args[0].equals("-t")) {
				System.out.println("semi test-run succesfull");
				return;
			}
		}

		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(LongMethodDetector.class.getName()).log(java.util.logging.Level.SEVERE,
					null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(LongMethodDetector.class.getName()).log(java.util.logging.Level.SEVERE,
					null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(LongMethodDetector.class.getName()).log(java.util.logging.Level.SEVERE,
					null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(LongMethodDetector.class.getName()).log(java.util.logging.Level.SEVERE,
					null, ex);
		}
		// </editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				// new LongMethodDetector().setVisible(true);
			}
		});
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JMenuItem detectLongMethodsMenuItem;
	private javax.swing.JTabbedPane jTabbedPane1;
	private javax.swing.JTable jTable1;
	private javax.swing.JLabel loadingAnimationJlabel;
	private javax.swing.JComboBox rankingMetricComboBox;
	private javax.swing.JLabel statusLabel;
	// End of variables declaration//GEN-END:variables

	private DefaultTableModel populateTable() { // ***POINT *DIAVASMA

		for (int i = 0; i < this.clazz.getMethods().size(); i++) {
			this.clazz.getMethods().get(i).printMetrics();
		}
		DefaultTableModel dtm = new DefaultTableModel(0, 0);
		DefaultTableModel treemodel = new DefaultTableModel(0, 0);
		String selected_metric = (String) this.rankingMetricComboBox.getSelectedItem();
		String header[] = new String[] { "Method name", selected_metric, "<html>Needs<br>refactoring</html>" };
		dtm.setColumnIdentifiers(header);
		treemodel.setColumnIdentifiers(header);
		jTable1.setModel(dtm);

		// text allignment
		DefaultTableCellRenderer header_renderer = (DefaultTableCellRenderer) jTable1.getTableHeader()
				.getDefaultRenderer();
		header_renderer.setHorizontalAlignment(JLabel.CENTER);
		JTableHeader table_header = jTable1.getTableHeader();
		table_header.setPreferredSize(new Dimension(100, 32));
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(JLabel.LEFT);
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		jTable1.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
		jTable1.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
		jTable1.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

		table_header.getColumnModel().getColumn(0).setPreferredWidth(000);
		table_header.getColumnModel().getColumn(1).setPreferredWidth(20);
		table_header.getColumnModel().getColumn(2).setPreferredWidth(20);
		jTable1.getColumnModel().getColumn(0).setPreferredWidth(200);
		jTable1.getColumnModel().getColumn(1).setPreferredWidth(20);
		jTable1.getColumnModel().getColumn(2).setPreferredWidth(20);

		clazz.sortMethodsOnMetric(selected_metric); // ***POINT

		double min = clazz.getMinValueOfMetric(selected_metric); // ***POINT
		double max = clazz.getMaxValueOfMetric(selected_metric); // ***POINT

		DecimalFormat formatter = new DecimalFormat("0.0000");
		for (int index = 0; index < clazz.getMethods().size(); index++) {
			boolean needsRefactoring = clazz.getMethods().get(index).needsRefactoring(selected_metric); // ***POINT
			String methodName = clazz.getMethods().get(index).getName();
			// modify name for output
			methodName = methodName.replace("(", " (").replace(",", ", ");
			double metric_value = clazz.getMethods().get(index).getMetricIndexFromName(selected_metric);
			// normalising the metric for using it in the HeatMap
			double norm = (metric_value - min) / (max - min);

			// metric is coh or cc get the opposite number for the ranking
			if (selected_metric.equals("COH") || selected_metric.equals("CC")) {
				treemodel.addRow(new Object[] { methodName, 1 - norm, needsRefactoring ? "yes" : "no" });
			} else {
				treemodel.addRow(new Object[] { methodName, norm, needsRefactoring ? "yes" : "no" });
			}
			String value_for_table = String.valueOf(metric_value).equals("NaN") ? "NaN"
					: formatter.format((Number) metric_value);// ***POINT
			dtm.addRow(new Object[] { methodName, value_for_table, needsRefactoring ? "yes" : "no" }); // ***POINT
		}
		jTable1.setRowSelectionInterval(0, 0);
		jTable1.changeSelection(0, 0, false, false);

		return treemodel;
	}

	void createTreeMap(DefaultTableModel dtm) {
		String selected_metric = (String) this.rankingMetricComboBox.getSelectedItem();
		// Creating the TreeMap
		treeMap = new TreeMap(dtm);

		// Tuning the appearance of the TreeMap
		treeMap.setAlgorithm(AlgorithmFactory.SQUARIFIED);
		treeMap.setSizeByName(selected_metric);
		treeMap.setColor(2);
		treeMap.setBackgroundByName("Method name");
		treeMap.setLabels();

		treeMap.getModel().getSettings().setShowPopup(treeMap.getModel().getTreeMapField(0), true);
		treeMap.getModel().getSettings().setRendering(RenderingFactory.CUSHION);
		treeMap.getModel().getSettings().getDefaultFieldSettings().setLabeling(LabelingFactory.SURROUND);

		treeMap.getModel().addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				try {
					String method_name = treeMap.getModel().getSelection().get(0).toString();
					int index = clazz.getMethodIndex(method_name);
					jTable1.setRowSelectionInterval(index, index);
					focusOnSelectedCell(index);
				} catch (Exception ex) {
					// FIXME: very often generated exception. Investigate different listeners for
					// this event
				}
			}

			private void focusOnSelectedCell(int index) {
				// scroll table to the selected coordinates
				jTable1.scrollRectToVisible(new Rectangle(jTable1.getCellRect(index, 0, true)));
			}
		});

		this.jTabbedPane1.add("Heatmap [" + selected_metric + "]", treeMap);
		if (this.jTabbedPane1.getComponentCount() > 1) {
			this.jTabbedPane1.remove(0);
		}

	}

	void performRanking() { // ***POINT *DIAVASMA
		String selected_metric = (String) this.rankingMetricComboBox.getSelectedItem();
		if (LongMethodDetector.DebugMode) {
			System.out.println("Ranking methods on " + selected_metric);
		}
		createTreeMap(populateTable());
	}

	class Task extends SwingWorker<Void, Void> {

		/*
		 * Main task. Executed in background thread.
		 */

		@Override
		public Void doInBackground() {
			try {
				ImageIcon loading_anim = new ImageIcon("img/loading.gif");
				Image anim = loading_anim.getImage().getScaledInstance(20, 20, 1);
				ImageIcon loading = new ImageIcon(anim);
				loadingAnimationJlabel.setIcon(loading);
				loadingAnimationJlabel.setVisible(true);
				statusLabel.setText("Analysing " + java_source_file.getName() + " ... [this might take some time]");
				detectLongMethodsMenuItem.setEnabled(false);

				clazz = analyser.performAnalysis();
				performRanking();

				statusLabel.setText("Analysing " + java_source_file.getName() + " completed.");
				// setTitle("Long method detector : " + java_source_file.getName());
				loadingAnimationJlabel.setVisible(false);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				detectLongMethodsMenuItem.setEnabled(true);
			}
			return null;

		}

		/*
		 * Executed in event dispatching thread
		 */
		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			// setCursor(null); //turn off the wait cursor
		}
	}
}
