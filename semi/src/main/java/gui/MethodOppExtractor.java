package gui;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import clustering.cluster.Opportunity;
import clustering.cluster.OpportunityList;
import splitlongmethod.ClusterList;
import splitlongmethod.JavaClass;
import splitlongmethod.Method;

/**
 *
 * @author Antonis Gkortzis (s2583070, antonis.gkortzis@gmail.com)
 */
public class MethodOppExtractor {

	private final File file;
	private final Method method;
	private final JavaClass clazz; // new
	private final String selected_method;
	public MethodOppExtractorSettings settings;
	public static String cohesion_metric = "lcom2";
	public static int cohesion_metric_index = 2;
	DecimalFormat format = new DecimalFormat("#.###");

	/**
	 * Creates new MethodOppEctractor Frame
	 *
	 * @param java_source_file
	 * @param clazz
	 * @param selected_method
	 * @param extractor_settings
	 */
	public MethodOppExtractor(File java_source_file, String selected_method,
			MethodOppExtractorSettings extractor_settings, JavaClass clazz) {
		// super("Extract Method Opportunities : " + selected_method);
		// setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		System.out.println("MethodOppExtractor started 1");
		this.file = java_source_file;
		this.settings = extractor_settings;
		this.settings.setParentFrame(this);
		this.selected_method = selected_method.replace(" (", "(").replace(", ", ","); // return to the original name of
																						// the method. It was changed in
																						// order to be more presentable
																						// in the Treemap table.
		format.setDecimalSeparatorAlwaysShown(false);

		this.method = clazz.getMethods().getMethodByName(this.selected_method);
		this.clazz = clazz;
		calcOportunities();

		System.out.println("MethodOppExtractor done 1");
		// initComponents();
		// loadFile();
		// initListenerForJlist();
		// initListenerForJTable();

		// task = new Task();
		// task.execute();
	}

	public Void calcOportunities() {

		System.out.println("calcOportunities started");
		try {
			if (!method.been_analysed) {
				clazz.identifyExtractMethodOpportunitiesForOneMethod(clazz.getInvalid_lines(),
						clazz.getPossible_invalid_bracket_close(), selected_method);
			}

			clusterOpportunitiesWithParameters();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		System.out.println("calcOportunities done");
		return null;
	}

	// Cosntructoe for the premade functionality
	public MethodOppExtractor(File inputFile, MethodOppExtractorSettings extractor_settings) {
		// super("Extract Method Opportunities : " + inputFile.getName());
		System.out.println("MethodOppExtractor started 2");
		file = null;
		clazz = null;
		selected_method = null;

		// setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		this.settings = extractor_settings;
		this.settings.setParentFrame(this);
		this.method = new Method(inputFile.getName());

		initComponents();
		initListenerForJlist();

		groupPreloadedOpporunities(inputFile);
		clusterOpportunitiesWithParameters();
		System.out.println("MethodOppExtractor done 2");
	}

	public void setSettings(MethodOppExtractorSettings settings) {
		this.settings = settings;
	}

	// <editor-fold defaultstate="collapsed" desc="Generated
	// Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		/*
		 * jMenuItem1 = new javax.swing.JMenuItem(); jSplitPane1 = new
		 * javax.swing.JSplitPane(); jPanel1 = new javax.swing.JPanel(); jScrollPane1 =
		 * new javax.swing.JScrollPane(); classSourceCodeJTextArea = new
		 * javax.swing.JTextArea(); jLabel4 = new javax.swing.JLabel(); jPanel2 = new
		 * javax.swing.JPanel(); jScrollPane2 = new javax.swing.JScrollPane(); jTable1 =
		 * new javax.swing.JTable(); jScrollPane3 = new javax.swing.JScrollPane();
		 * jList1 = new javax.swing.JList(); jLabel1 = new javax.swing.JLabel(); jLabel2
		 * = new javax.swing.JLabel(); statusLabel = new javax.swing.JLabel();
		 * loadingAnimationJlabel = new javax.swing.JLabel(); jMenuBar1 = new
		 * javax.swing.JMenuBar(); jMenu1 = new javax.swing.JMenu(); jSeparator1 = new
		 * javax.swing.JPopupMenu.Separator(); quitJMenuItem = new
		 * javax.swing.JMenuItem(); jMenu2 = new javax.swing.JMenu(); jMenuItem2 = new
		 * javax.swing.JMenuItem();
		 * 
		 * jMenuItem1.setText("jMenuItem1");
		 * 
		 * setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		 * addWindowListener(new java.awt.event.WindowAdapter() { public void
		 * windowClosing(java.awt.event.WindowEvent evt) { formWindowClosing(evt); } });
		 * 
		 * jSplitPane1.setDividerLocation(540); jSplitPane1.setPreferredSize(new
		 * java.awt.Dimension(900, 275));
		 * 
		 * classSourceCodeJTextArea.setEditable(false);
		 * classSourceCodeJTextArea.setColumns(20); classSourceCodeJTextArea.setRows(5);
		 * jScrollPane1.setViewportView(classSourceCodeJTextArea);
		 * 
		 * jLabel4.setText("jLabel4");
		 * 
		 * javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		 * jPanel1.setLayout(jPanel1Layout); jPanel1Layout.setHorizontalGroup(
		 * jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		 * .addGroup(jPanel1Layout.createSequentialGroup() .addContainerGap()
		 * .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment
		 * .LEADING) .addComponent(jScrollPane1,
		 * javax.swing.GroupLayout.Alignment.TRAILING,
		 * javax.swing.GroupLayout.DEFAULT_SIZE, 612, Short.MAX_VALUE)
		 * .addGroup(jPanel1Layout.createSequentialGroup() .addComponent(jLabel4)
		 * .addGap(0, 0, Short.MAX_VALUE)))) ); jPanel1Layout.setVerticalGroup(
		 * jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		 * .addGroup(jPanel1Layout.createSequentialGroup() .addGap(20, 20, 20)
		 * .addComponent(jLabel4)
		 * .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		 * .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 438,
		 * Short.MAX_VALUE) .addGap(33, 33, 33)) );
		 * 
		 * jSplitPane1.setRightComponent(jPanel1);
		 * 
		 * jTable1.setModel(new javax.swing.table.DefaultTableModel( new Object [][] {
		 * {null, null, null, null}, {null, null, null, null}, {null, null, null, null},
		 * {null, null, null, null} }, new String [] { "lines of code",
		 * "cohesion metric (before)", "cohesion metric (after)",
		 * "cohesion metric (new_method)" } ) { Class[] types = new Class [] {
		 * java.lang.String.class, java.lang.Double.class, java.lang.Double.class,
		 * java.lang.Double.class };
		 * 
		 * public Class getColumnClass(int columnIndex) { return types [columnIndex]; }
		 * }); jScrollPane2.setViewportView(jTable1);
		 * 
		 * jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		 * jScrollPane3.setViewportView(jList1);
		 * 
		 * jLabel1.setText("Opportunities");
		 * 
		 * jLabel2.setText("Similar Oportunities");
		 * 
		 * statusLabel.setText("status label");
		 * 
		 * javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		 * jPanel2.setLayout(jPanel2Layout); jPanel2Layout.setHorizontalGroup(
		 * jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		 * .addGroup(jPanel2Layout.createSequentialGroup() .addContainerGap()
		 * .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment
		 * .LEADING) .addGroup(jPanel2Layout.createSequentialGroup()
		 * .addComponent(loadingAnimationJlabel)
		 * .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		 * .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 446,
		 * javax.swing.GroupLayout.PREFERRED_SIZE) .addGap(0, 67, Short.MAX_VALUE))
		 * .addGroup(jPanel2Layout.createSequentialGroup()
		 * .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment
		 * .LEADING) .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE,
		 * 228, javax.swing.GroupLayout.PREFERRED_SIZE) .addComponent(jLabel1))
		 * .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
		 * .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment
		 * .LEADING) .addGroup(jPanel2Layout.createSequentialGroup()
		 * .addComponent(jLabel2) .addGap(0, 0, Short.MAX_VALUE))
		 * .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0,
		 * Short.MAX_VALUE)))) .addContainerGap()) ); jPanel2Layout.setVerticalGroup(
		 * jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		 * .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
		 * jPanel2Layout.createSequentialGroup() .addGap(19, 19, 19)
		 * .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment
		 * .BASELINE) .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE,
		 * javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE) .addComponent(jLabel2,
		 * javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
		 * Short.MAX_VALUE))
		 * .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		 * .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment
		 * .LEADING) .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE,
		 * 441, Short.MAX_VALUE) .addComponent(jScrollPane3))
		 * .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
		 * .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment
		 * .BASELINE) .addComponent(statusLabel) .addComponent(loadingAnimationJlabel))
		 * .addGap(6, 6, 6)) );
		 * 
		 * loadingAnimationJlabel.getAccessibleContext().setAccessibleName(
		 * "loadingAnimationJlabel");
		 * 
		 * jSplitPane1.setLeftComponent(jPanel2);
		 * 
		 * jMenu1.setText("File"); jMenu1.add(jSeparator1);
		 * 
		 * quitJMenuItem.setText("Quit"); quitJMenuItem.addActionListener(new
		 * java.awt.event.ActionListener() { public void
		 * actionPerformed(java.awt.event.ActionEvent evt) {
		 * quitJMenuItemActionPerformed(evt); } }); jMenu1.add(quitJMenuItem);
		 * 
		 * jMenuBar1.add(jMenu1);
		 * 
		 * jMenu2.setText("Settings");
		 * 
		 * jMenuItem2.setText("Clustering settings"); jMenuItem2.addActionListener(new
		 * java.awt.event.ActionListener() { public void
		 * actionPerformed(java.awt.event.ActionEvent evt) {
		 * jMenuItem2ActionPerformed(evt); } }); jMenu2.add(jMenuItem2);
		 * 
		 * jMenuBar1.add(jMenu2);
		 * 
		 * setJMenuBar(jMenuBar1);
		 * 
		 * javax.swing.GroupLayout layout = new
		 * javax.swing.GroupLayout(getContentPane());
		 * getContentPane().setLayout(layout); layout.setHorizontalGroup(
		 * layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		 * .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1168,
		 * Short.MAX_VALUE) ); layout.setVerticalGroup(
		 * layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		 * .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 513,
		 * Short.MAX_VALUE) );
		 * 
		 * pack();
		 */
	}// </editor-fold>//GEN-END:initComponents

	public void clusterOpportunitiesWithParameters() {

		this.method.getOpportunityList().printOptimals();

		if (LongMethodDetector.DebugMode) {
			System.out.println(
					"Total optimal opps: " + this.method.getOpportunityList().getNumberOfOpportunitySuggestions());
		}
	}

	private void initListenerForJlist() {
		/*
		 * ListSelectionListener listSelectionListener = new ListSelectionListener() {
		 * public void valueChanged(ListSelectionEvent listSelectionEvent) { // The
		 * adjust thingy helps as avoid duplicated events boolean adjust =
		 * listSelectionEvent.getValueIsAdjusting(); if (!adjust &&
		 * jList1.getSelectedValue() != null) { // create object of table and table
		 * model DefaultTableModel dtm = new DefaultTableModel(0, 0); // add header of
		 * the table
		 * 
		 * String header[] = new String[]{"lines of code", "<html>" +
		 * settings.getCohesionMetric() + "<br>" + " (benefit)" + "</html>", "<html>" +
		 * settings.getCohesionMetric() + "<br>" + " (before)" + "</html>", "<html>" +
		 * settings.getCohesionMetric() + "<br>" + " (after)" + "</html>", "<html>" +
		 * settings.getCohesionMetric() + "<br>" + " (new_method)" + "</html>"}; // add
		 * header in table model
		 * 
		 * //dtm.setColumnIdentifiers(header); String list_value =
		 * jList1.getSelectedValue().toString();
		 * 
		 * //list_value = list_value.substring(0, list_value.indexOf(" (" +
		 * settings.getCohesionMetric() + " benefit")); //Add the current opp in the
		 * first row of the table Opportunity opp =
		 * method.getOpportunityList().getOpportunity(list_value); dtm.addRow(new
		 * Object[]{opp.getLinesCluster(), //lines cluster
		 * format.format(opp.getOpportunityBenefitMetricByName(cohesion_metric)),
		 * //metric benefit
		 * format.format(opp.getOpportunityOriginalCohesionByName(cohesion_metric)),
		 * //metric before
		 * format.format(opp.getOpportunityOriginalCohesionByName(cohesion_metric) -
		 * opp.getOpportunityDeltaMetricByName(cohesion_metric)), //metric after
		 * format.format(opp.getOpportunityCohesionByMetric(cohesion_metric))});
		 * 
		 * OpportunityList cluster = opp.getCluster(); for (int i = 0; i <
		 * cluster.size(); i++) { opp = cluster.get(i); dtm.addRow(new
		 * Object[]{opp.getLinesCluster(), //lines cluster
		 * format.format(opp.getOpportunityBenefitMetricByName(cohesion_metric)),
		 * //metric benefit
		 * format.format(opp.getOpportunityOriginalCohesionByName(cohesion_metric)),
		 * //metric before
		 * format.format(opp.getOpportunityOriginalCohesionByName(cohesion_metric) -
		 * opp.getOpportunityDeltaMetricByName(cohesion_metric)), //metric after
		 * format.format(opp.getOpportunityCohesionByMetric(cohesion_metric))}); }
		 * jTable1.getTableHeader().setPreferredSize(new
		 * Dimension(jTable1.getColumnModel().getTotalColumnWidth(), 32));
		 * //compgroups.net/comp.lang.java.gui/how-do-i-set-table-header-height-without-
		 * s/188064#sthash.Fk7bETVa.dpuf
		 * 
		 * //add the new model to the table jTable1.setModel(dtm);
		 * 
		 * //automatically select the 1st opportunity if (dtm.getRowCount() > 0) {
		 * jTable1.setRowSelectionInterval(0, 0); } } } };
		 * this.jList1.addListSelectionListener(listSelectionListener);
		 */
	}

	private void loadPremadeOpportunities(File inputFile) throws IOException {
		if (LongMethodDetector.DebugMode) {
			System.out.println("## LOADING PREMADE OPPS ##");
		}
		OpportunityList opplist = new OpportunityList();

		String line = "";
		int cnt = 0;
		String[] original_metrics = new String[17];
		boolean first_pass = true;

		try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
			while ((line = br.readLine()) != null) {
				String[] metrics = line.split(",");

				String[] cluster_metrics = new String[19];
				String[] new_metrics = new String[17];

				if (first_pass) {
					cnt = 2;
					original_metrics[0] = Integer
							.toString(Integer.parseInt(metrics[0].substring(0, metrics[0].indexOf(' '))) - 1);
					original_metrics[1] = Integer
							.toString(Integer.parseInt(metrics[0].substring(metrics[0].indexOf(" to ") + 4)) - 1);
					for (int i = 1; i <= 15; i++) {
						original_metrics[cnt++] = metrics[i];
					}
				}

				// TODO get the Lines start-end at positions 0 & 1 of cluster
				cnt = 2;
				cluster_metrics[0] = Integer
						.toString(Integer.parseInt(metrics[16].substring(0, metrics[16].indexOf(' '))) - 1);
				cluster_metrics[1] = Integer
						.toString(Integer.parseInt(metrics[16].substring(metrics[16].indexOf(" to ") + 4)) - 1);
				for (int i = 17; i <= 33; i++) {
					cluster_metrics[cnt++] = metrics[i];
				}

				// TODO get the Lines start-end at positions 0 & 1 of new
				cnt = 2;
				if (metrics.length > 35) {
					if (metrics[34].equals("to")) {
						new_metrics[0] = Integer.toString(0);
						new_metrics[1] = Integer.toString(0);
					} else {
						new_metrics[0] = Integer
								.toString(Integer.parseInt(metrics[34].substring(0, metrics[34].indexOf(' '))) - 1);
						new_metrics[1] = Integer
								.toString(Integer.parseInt(metrics[34].substring(metrics[34].indexOf(" to ") + 4)) - 1);
					}
					for (int i = 35; i <= 49; i++) {
						new_metrics[cnt++] = metrics[i];
					}
				} else {
					cnt = 3;
					new_metrics[0] = Integer.toString(0);
					new_metrics[1] = Integer.toString(0);
					new_metrics[2] = Integer.toString(0);
					for (int i = 36; i <= 49; i++) {
						new_metrics[cnt++] = "NaN";
					}
				}

				ClusterList cluster = new ClusterList();
				Opportunity opp = cluster.parseMetrics(original_metrics, cluster_metrics, new_metrics);
				opplist.add(opp);
				opplist.calculateBenefits();
				opplist.addBenefitsToList();
			}
		} catch (Exception ex) {
			System.err.println("failed line : " + line);
		}

		this.method.setOppList(opplist);
	}

	class Task extends SwingWorker<Void, Void> {

		@Override
		public Void doInBackground() {
			try {
				if (!method.been_analysed) {
					clazz.identifyExtractMethodOpportunitiesForOneMethod(clazz.getInvalid_lines(),
							clazz.getPossible_invalid_bracket_close(), selected_method);
				}

				clusterOpportunitiesWithParameters();

				// statusLabel.setText("Analysing " + method.getName() + " completed.");
				// loadingAnimationJlabel.setVisible(false);
			} catch (Exception ex) {
				ex.printStackTrace();
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

	void groupPreloadedOpporunities(File inputFile) {
		// setVisible(true);
		try {
			loadPremadeOpportunities(inputFile);
		} catch (IOException ex) {
			Logger.getLogger(MethodOppExtractor.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
