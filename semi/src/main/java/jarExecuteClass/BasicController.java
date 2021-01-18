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
//			return; // ***POINT TEST_COM
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
//			return false; // ***POINT TEST_COM
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
			System.out.println("opps: "+sss);
		}
		
		boolean result = true;
		for (int t = 0; t < threadNum; t++) {
			result = result && threads[t].isSuccessful();
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
}
