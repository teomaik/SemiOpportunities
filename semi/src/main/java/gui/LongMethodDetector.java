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
		/*
		 * statusBarPanel = new javax.swing.JPanel(); statusLabel = new
		 * javax.swing.JLabel(); jSplitPane1 = new javax.swing.JSplitPane(); jPanel1 =
		 * new javax.swing.JPanel(); jScrollPane1 = new javax.swing.JScrollPane();
		 * jTable1 = new javax.swing.JTable(); jLabel1 = new javax.swing.JLabel();
		 * identifyButton = new javax.swing.JButton(); rankingMetricComboBox = new
		 * javax.swing.JComboBox(); jLabel2 = new javax.swing.JLabel(); jTabbedPane1 =
		 * new javax.swing.JTabbedPane(); loadingAnimationJlabel = new
		 * javax.swing.JLabel(); jMenuBar1 = new javax.swing.JMenuBar(); fileMenu = new
		 * javax.swing.JMenu(); loadClassMenuItem = new javax.swing.JMenuItem();
		 * detectLongMethodsMenuItem = new javax.swing.JMenuItem(); jSeparator1 = new
		 * javax.swing.JPopupMenu.Separator(); quitApplicationMenuItem = new
		 * javax.swing.JMenuItem(); settingsMenu = new javax.swing.JMenu(); jMenuItem1 =
		 * new javax.swing.JMenuItem(); jMenuItem2 = new javax.swing.JMenuItem();
		 * 
		 * setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		 * addWindowListener(new java.awt.event.WindowAdapter() { public void
		 * windowClosing(java.awt.event.WindowEvent evt) { formWindowClosing(evt); } });
		 * 
		 * statusLabel.
		 * setText(" Select a class [File->Load class] to start the long method detection."
		 * );
		 * 
		 * javax.swing.GroupLayout statusBarPanelLayout = new
		 * javax.swing.GroupLayout(statusBarPanel);
		 * statusBarPanel.setLayout(statusBarPanelLayout);
		 * statusBarPanelLayout.setHorizontalGroup(
		 * statusBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.
		 * LEADING) .addGroup(statusBarPanelLayout.createSequentialGroup()
		 * .addComponent(statusLabel) .addGap(0, 0, Short.MAX_VALUE)) );
		 * statusBarPanelLayout.setVerticalGroup(
		 * statusBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.
		 * LEADING) .addGroup(statusBarPanelLayout.createSequentialGroup()
		 * .addComponent(statusLabel) .addGap(0, 11, Short.MAX_VALUE)) );
		 * 
		 * jSplitPane1.setDividerLocation(600);
		 * 
		 * jTable1.setModel(new javax.swing.table.DefaultTableModel( new Object [][] {
		 * 
		 * }, new String [] { "Method name", "lcom2", "Needs refactoring" } ) { Class[]
		 * types = new Class [] { java.lang.String.class, java.lang.String.class,
		 * java.lang.String.class }; boolean[] canEdit = new boolean [] { false, false,
		 * false };
		 * 
		 * public Class getColumnClass(int columnIndex) { return types [columnIndex]; }
		 * 
		 * public boolean isCellEditable(int rowIndex, int columnIndex) { return canEdit
		 * [columnIndex]; } }); jTable1.setColumnSelectionAllowed(true);
		 * jTable1.getTableHeader().setReorderingAllowed(false);
		 * jScrollPane1.setViewportView(jTable1);
		 * jTable1.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.
		 * ListSelectionModel.SINGLE_SELECTION); if
		 * (jTable1.getColumnModel().getColumnCount() > 0) {
		 * jTable1.getColumnModel().getColumn(0).setPreferredWidth(200);
		 * jTable1.getColumnModel().getColumn(1).setPreferredWidth(20);
		 * jTable1.getColumnModel().getColumn(2).setResizable(false);
		 * jTable1.getColumnModel().getColumn(2).setPreferredWidth(20); }
		 * 
		 * jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		 * jLabel1.setText("Ranking: Urgency for refactoring");
		 * 
		 * identifyButton.setText("Identify Extract Method Opportunities");
		 * identifyButton.addActionListener(new java.awt.event.ActionListener() { public
		 * void actionPerformed(java.awt.event.ActionEvent evt) {
		 * identifyButtonActionPerformed(evt); } });
		 * 
		 * rankingMetricComboBox.setModel(new javax.swing.DefaultComboBoxModel(new
		 * String[] { "SIZE", "LCOM1", "LCOM2", "LCOM4", "COH", "CC" }));
		 * rankingMetricComboBox.addItemListener(new java.awt.event.ItemListener() {
		 * public void itemStateChanged(java.awt.event.ItemEvent evt) {
		 * rankingMetricComboBoxItemStateChanged(evt); } });
		 * rankingMetricComboBox.addPropertyChangeListener(new
		 * java.beans.PropertyChangeListener() { public void
		 * propertyChange(java.beans.PropertyChangeEvent evt) {
		 * rankingMetricComboBoxPropertyChange(evt); } });
		 * 
		 * jLabel2.setText("Rank on metric:");
		 * 
		 * javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		 * jPanel1.setLayout(jPanel1Layout); jPanel1Layout.setHorizontalGroup(
		 * jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		 * .addGroup(jPanel1Layout.createSequentialGroup() .addContainerGap()
		 * .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment
		 * .LEADING) .addGroup(jPanel1Layout.createSequentialGroup()
		 * .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment
		 * .LEADING) .addComponent(jScrollPane1,
		 * javax.swing.GroupLayout.Alignment.TRAILING,
		 * javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
		 * .addComponent(identifyButton, javax.swing.GroupLayout.Alignment.TRAILING,
		 * javax.swing.GroupLayout.DEFAULT_SIZE, 1018, Short.MAX_VALUE))
		 * .addContainerGap()) .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
		 * jPanel1Layout.createSequentialGroup() .addGap(0, 0, Short.MAX_VALUE)
		 * .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment
		 * .LEADING) .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
		 * jPanel1Layout.createSequentialGroup() .addComponent(jLabel1) .addGap(129,
		 * 129, 129)) .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
		 * jPanel1Layout.createSequentialGroup() .addComponent(jLabel2)
		 * .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		 * .addComponent(rankingMetricComboBox, javax.swing.GroupLayout.PREFERRED_SIZE,
		 * 88, javax.swing.GroupLayout.PREFERRED_SIZE) .addGap(20, 20, 20)))))) );
		 * jPanel1Layout.setVerticalGroup(
		 * jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		 * .addGroup(jPanel1Layout.createSequentialGroup() .addGap(5, 5, 5)
		 * .addComponent(jLabel1)
		 * .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		 * .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 454,
		 * Short.MAX_VALUE)
		 * .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
		 * .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment
		 * .BASELINE) .addComponent(rankingMetricComboBox,
		 * javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
		 * javax.swing.GroupLayout.PREFERRED_SIZE) .addComponent(jLabel2)) .addGap(18,
		 * 18, 18) .addComponent(identifyButton, javax.swing.GroupLayout.PREFERRED_SIZE,
		 * 35, javax.swing.GroupLayout.PREFERRED_SIZE) .addGap(4, 4, 4)) );
		 * 
		 * jSplitPane1.setRightComponent(jPanel1);
		 * jSplitPane1.setLeftComponent(jTabbedPane1);
		 * 
		 * fileMenu.setText("File");
		 * 
		 * loadClassMenuItem.setText("Load class");
		 * loadClassMenuItem.addActionListener(new java.awt.event.ActionListener() {
		 * public void actionPerformed(java.awt.event.ActionEvent evt) {
		 * loadClassMenuItemActionPerformed(evt); } }); fileMenu.add(loadClassMenuItem);
		 * 
		 * detectLongMethodsMenuItem.setText("Detect long methods");
		 * detectLongMethodsMenuItem.addActionListener(new
		 * java.awt.event.ActionListener() { public void
		 * actionPerformed(java.awt.event.ActionEvent evt) {
		 * detectLongMethodsMenuItemActionPerformed(evt); } });
		 * fileMenu.add(detectLongMethodsMenuItem); fileMenu.add(jSeparator1);
		 * 
		 * quitApplicationMenuItem.setText("Quit application");
		 * quitApplicationMenuItem.addActionListener(new java.awt.event.ActionListener()
		 * { public void actionPerformed(java.awt.event.ActionEvent evt) {
		 * quitApplicationMenuItemActionPerformed(evt); } });
		 * fileMenu.add(quitApplicationMenuItem);
		 * 
		 * jMenuBar1.add(fileMenu);
		 * 
		 * settingsMenu.setText("Settings");
		 * 
		 * jMenuItem1.setText("Ranking settigns"); jMenuItem1.addActionListener(new
		 * java.awt.event.ActionListener() { public void
		 * actionPerformed(java.awt.event.ActionEvent evt) {
		 * jMenuItem1ActionPerformed(evt); } }); settingsMenu.add(jMenuItem1);
		 * 
		 * jMenuItem2.setText("Load premade opps"); jMenuItem2.addActionListener(new
		 * java.awt.event.ActionListener() { public void
		 * actionPerformed(java.awt.event.ActionEvent evt) {
		 * jMenuItem2ActionPerformed(evt); } }); settingsMenu.add(jMenuItem2);
		 * 
		 * jMenuBar1.add(settingsMenu);
		 * 
		 * setJMenuBar(jMenuBar1);
		 * 
		 * javax.swing.GroupLayout layout = new
		 * javax.swing.GroupLayout(getContentPane());
		 * getContentPane().setLayout(layout); layout.setHorizontalGroup(
		 * layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		 * .addGroup(layout.createSequentialGroup() .addGap(7, 7, 7)
		 * .addComponent(loadingAnimationJlabel)
		 * .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		 * .addComponent(statusBarPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
		 * javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		 * .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING) );
		 * layout.setVerticalGroup(
		 * layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		 * .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
		 * layout.createSequentialGroup() .addComponent(jSplitPane1)
		 * .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		 * .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.
		 * LEADING) .addComponent(statusBarPanel,
		 * javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
		 * javax.swing.GroupLayout.PREFERRED_SIZE)
		 * .addComponent(loadingAnimationJlabel))) );
		 * 
		 * pack();
		 */
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

		/* Set the Nimbus look and feel */
		// <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
		// (optional) ">
		/*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the default
		 * look and feel. For details see
		 * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
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
