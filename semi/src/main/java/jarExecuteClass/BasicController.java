package jarExecuteClass;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import AST.ClassParser;
import clustering.cluster.Opportunity;
import db.DbController;
import gui.Analyser;
import gui.MethodOppExtractor;
import gui.MethodOppExtractorSettings;
import jarExecuteClass.parallelSemi.ActiveFiles;
import jarExecuteClass.parallelSemi.SemiThread;
import splitlongmethod.JavaClass;
import splitlongmethod.Method;

public class BasicController {

	DbController dbCon = null;

	private String projectName = null;
	private String projectProgramingLanguage = null;
	private String projectDirectoryPath = null;
	private String credPath = null;

	// private ArrayList<JavaClass> classResults = new ArrayList<>();
	private String selected_metric = "SIZE";// "LCOM1", "LCOM2", "LCOM4", "COH", "CC" //***???

	private int number_of_refs = 0;

	// Java projects
	private ArrayList<File> java_source_files = new ArrayList<>();
	private Analyser analyser = null;
	JavaClass javaClass = null;
	public static boolean DebugMode = false;
	public static String cohesion_metric = "LCOM2";
	public static int cohesion_metric_index = 2;

	private int C_ProjectVersion = 999;

	public BasicController(String type, String projectName, String C_ProjectVersion, String directoryPath,
			String dbCredPath) {
		if (type == null || type.isEmpty() || type.trim().length() == 0 || projectName == null || projectName.isEmpty()
				|| projectName.trim().length() == 0 || C_ProjectVersion == null || C_ProjectVersion.isEmpty()
				|| C_ProjectVersion.trim().length() == 0 || directoryPath == null || directoryPath.isEmpty()
				|| directoryPath.trim().length() == 0 || dbCredPath == null || dbCredPath.isEmpty()
				|| dbCredPath.trim().length() == 0) {
			return;
		}
		if (!type.equals("c") && !type.equals("cpp") && !type.equals("java")) {
			return;
		}

		credPath = dbCredPath;
		this.C_ProjectVersion = Integer.valueOf(C_ProjectVersion);
		dbCon = new DbController(dbCredPath);
		if (!dbCon.isReady()) {
			System.out.println("Problem with databaseConnection");
			return; // ***POINT TEST_COM
		}
		this.projectProgramingLanguage = type;
		this.projectName = projectName;
		this.projectDirectoryPath = directoryPath;

		analyser = new Analyser();

	}

	public boolean runExperiment() {
		System.out.println("expStarted"); // ***DEBUG
		if (this.dbCon == null || !this.dbCon.isReady()) {
			System.out.println("Problem with databaseConnection");
			return false; // ***POINT TEST_COM
		}

		dbCon.closeConn();

		boolean commit = getFilesForAnalysis();
		
//		int debug=0;
//		debug = 1/debug;

		if (!commit) {
			System.out.println("Something went wrong with the file analysis");
			return false;
		}

		writeLogSkippedFiles();
		dbCon.getNewConnection(credPath);

		if (!this.dbCon.isReady()) {
			System.out.println("Problem with databaseConnection");
			return false;
		}
		System.out.println("Number of refs: " + number_of_refs);

		commit = commit && dbCon.dbActions(projectName, C_ProjectVersion);

		if (commit) {
			dbCon.connCommitAndClose();
			System.out.println("FIN!");
			return true;
		} else {
			dbCon.connRollBackAndClose();
			return false;
		}
	}

	ArrayList<String> opps = new ArrayList<String>();

	private ActiveFiles filesForAnalysis = new ActiveFiles();

	private boolean getFilesForAnalysis() {
		File directory = new File(projectDirectoryPath);
		if (!directory.exists() || !directory.isDirectory()) {
			return false;
		}

		if (projectProgramingLanguage.equals("java")) {
			getFilesForAnalysis_Java_TEMP(projectDirectoryPath);
			//TODO do parallel part for java
			//return doJavaAnalysis();
		} else if (projectProgramingLanguage.equals("c") || projectProgramingLanguage.equals("cpp")) {
			getFilesForAnalysis_C_TEMP(projectDirectoryPath);
		} else {
			System.out.println("Wrong programing language. Only java, c or cpp are supported");
			return false;
		}

		int threadNum = Runtime.getRuntime().availableProcessors();
		if (threadNum <= 0) {
			System.out.println("Unexpected error, trouble determining thread number");
			System.exit(1);
		}

		SemiThread[] threads = new SemiThread[threadNum];
		for (int t = 0; t < threadNum; t++) {
			threads[t] = new SemiThread(filesForAnalysis, selected_metric, projectProgramingLanguage, opps, dbCon,
					projectName, skippedFiles, projectDirectoryPath);
			// System.out.println(t+"---> start:" + start + ", end:" + end);
		}

		for (int t = 0; t < threadNum; t++) {
			try {
				threads[t].join();
			} catch (InterruptedException e) {
			}
		}
		System.out.println("after join\n\n");
		
		for(String sss : opps) {
			System.out.println("GLOB_opp: "+sss);
		}
		
		boolean result = true;
		for (int t = 0; t < threadNum; t++) {
			result = result && threads[t].isSuccessful();
		}
		
		for (int t = 0; t < threadNum; t++) {
			threads[t].debug();
		}
		
		System.out.println("ACT ALL GOOD?: "+filesForAnalysis.debugAllGood());
		
		return result;
	}

	private ArrayList<String> skippedFiles = new ArrayList<String>();

	private void writeLogSkippedFiles() {
		String log = "";

		for (String file : this.skippedFiles) {
			log += file + "\n";
		}

		try (PrintWriter out = new PrintWriter("skippedFilesLog.txt")) {
			out.println(log);
		} catch (Exception e) {

		}
	}

	private void getFilesForAnalysis_Java_TEMP(String directoryPath) {

		File directory = new File(directoryPath);

		// Get all files from a directory.
		File[] fList = directory.listFiles();
		if (fList != null) {
			for (File file : fList) {
				if (file.isFile() && file.getName().contains(".") && file.getName().charAt(0) != '.') {
					String[] str = file.getName().split("\\.");
					// For all the filles of this dirrecory get the extension
					if (str[str.length - 1].equalsIgnoreCase("java")) {
						filesForAnalysis.addNewFile(file.getAbsolutePath());
					}
				} else if (file.isDirectory()) {
					getFilesForAnalysis_Java_TEMP(file.getAbsolutePath());
				}
			}
		}
	}

	private void getFilesForAnalysis_C_TEMP(String directoryName) {
		File directory = new File(directoryName);
		// Get all files from a directory.
		File[] fList = directory.listFiles();
		if (fList != null) {
			for (File file : fList) {
				if (file.isFile() && file.getName().contains(".") && file.getName().charAt(0) != '.') {
					String[] str = file.getName().split("\\.");
					if ((str[str.length - 1].equalsIgnoreCase("c")) || (str[str.length - 1].equalsIgnoreCase("cpp"))
							|| (str[str.length - 1].equalsIgnoreCase("cc"))
							|| (str[str.length - 1].equalsIgnoreCase("cp"))
							|| (str[str.length - 1].equalsIgnoreCase("cxx"))
							|| (str[str.length - 1].equalsIgnoreCase("c++"))
							|| (str[str.length - 1].equalsIgnoreCase("cu"))) {
						filesForAnalysis.addNewFile(file.getAbsolutePath());
					}

				} else if (file.isDirectory()) {
					System.out.println("********* file is folder");
					getFilesForAnalysis_C_TEMP(file.getAbsolutePath());
				}
			}
		}
	}

	
	private boolean doJavaAnalysis() {
		boolean ret = true;
		for(String filePath : this.filesForAnalysis.getFilePaths()) {
			File file = new File(filePath);
			ClassParser parser = new ClassParser(file.getAbsolutePath());
		
			parser.parse();
			utils.Utilities.writeCSV("./" + file.getName() + "_original_parsed.txt", parser.getOutput(),
					false);
			utils.Utilities.writeCSV("./" + file.getName() + "_parsed.txt", parser.getOutput(), false);

			try {
				ret = ret && doAnalysis(file);
			} catch (Exception e) {
				System.out.println("File analysis failed, " + e.getMessage());
				try {
					File fileDel = new File("./" + file.getName() + "_parsed.txt");
					fileDel.delete(); // ***POINT TEST_COM
				} catch (Exception exc) {
					System.out.println("Exception while deleting file");
				}
			}
		}
		return ret;
	}

	private MethodOppExtractorSettings settings = null;
	private boolean doAnalysis(File file) {
		// ***************************************************************************************************
		// <
		boolean ret = true;

		analyser.setFile(file);

		// classResults.add(analyser.performAnalysis()); //***TEMPCOM

		try { // ***TEST
			JavaClass clazz = analyser.performAnalysis();

			for (int index = 0; index < clazz.getMethods().size(); index++) {
				boolean needsRefactoring = clazz.getMethods().get(index).needsRefactoring(selected_metric);

				if (needsRefactoring) {

					if (clazz.getMethods().get(index).getMetricIndexFromName("size") < 50) {// skip methods with less
																							// line
																							// // of code
						continue;
					}
					String className = file.getName().replaceFirst("./", "");
					String classPath = getMeCorrectNameFormat(file.getAbsolutePath());
					String methodName = clazz.getMethods().get(index).getName();
					settings = new MethodOppExtractorSettings();
					MethodOppExtractor extractor = new MethodOppExtractor(file, clazz.getMethods().get(index).getName(),
							settings, clazz);

					Method method = clazz.getMethods().getMethodByName(methodName);
					ArrayList<Opportunity> opportunities = method.getOpportunityList().getOptimals();


					int count = 1;
					for (Opportunity opp : opportunities) {
						if (count > 1) {
							break;
						}
						number_of_refs++;
						opps.add(className + "." + methodName + " -> " + opp.getStartLineCluster() + "-"
								+ opp.getEndLineCluster() + " : " + opp.getOpportunityBenefitMetricByName("lcom2"));
						ret = ret && dbCon.insertMethodToDatabase(projectName, className, methodName,
								opp.getStartLineCluster(), opp.getEndLineCluster(),
								opp.getOpportunityBenefitMetricByName("lcom2"), method.getMetricIndexFromName("lcom2"),
								method.getMetricIndexFromName("size"), classPath);
						if(ret==false) {

							System.out.println("********ERROR sto DB insert");
							System.out.println("-----projectName "+projectName);
							System.out.println("-----className "+className);
							System.out.println("-----methodName "+methodName);
							System.out.println("-----opp.getStartLineCluster() "+opp.getStartLineCluster());
							System.out.println("-----opp.getEndLineCluster() "+opp.getEndLineCluster());
							System.out.println("-----opp.lcom2 "+opp.getOpportunityBenefitMetricByName("lcom2"));
							System.out.println("-----method.lcom2 "+method.getMetricIndexFromName("lcom2"));
							System.out.println("-----method.loc "+method.getMetricIndexFromName("size"));
							System.out.println("-----classPath "+classPath);
						}
						count++;
					}
				}
			}
		} catch (OutOfMemoryError E) {
			skippedFiles.add(file.getAbsolutePath());
			return true;
		}
		return ret;
	}
	
	private String getMeCorrectNameFormat(String oldName) {
		String retName = oldName.replace("\\", "/");
		retName = retName.replace("\\", "/");

		File file = new File(projectDirectoryPath);
		String simpleFileName = file.getName();
		String baseDirectory = projectDirectoryPath;// .replace(projectDirectoryPath, simpleFileName);
		baseDirectory = baseDirectory.replace("\\", "/");
		baseDirectory = baseDirectory.replace(simpleFileName, "");

		retName = retName.replaceFirst(baseDirectory, "");

		return retName;
	}
}
