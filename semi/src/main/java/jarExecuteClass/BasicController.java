package jarExecuteClass;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import AST.ClassParser;
import clustering.cluster.Opportunity;
import db.DbController;
import gui.Analyser;
import gui.MethodOppExtractor;
import gui.MethodOppExtractorSettings;
import parsers.CodeFile;
import parsers.cFile;
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
			//return; // ***POINT TEST_COM
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
			//return false; // ***POINT TEST_COM
		}

		dbCon.closeConn();

		boolean commit = true;

		System.out.println("b-sw"); // ***DEBUG
		switch (projectProgramingLanguage) {
		case "java":
			commit = doAnalysis_Java(projectDirectoryPath);
			break;
		case "c":
			commit = doAnalysis_C_Cpp(projectDirectoryPath);
			break;
		case "cpp":
			commit = doAnalysis_C_Cpp(projectDirectoryPath);
			break;
		// case "f":
		// commit = doAnalysis_F(projectDirectoryPath);
		// break;
		// case "f90":
		// commit = doAnalysis_F(projectDirectoryPath);
		// break;

		default:
			System.out.println("wrong argument for Programing Language (java, c, f, f90)");
		}
		System.out.println("a-sw"); // ***DEBUG
		if (!commit) {
			System.out.println("Something went wrong with the file analysis");
			return false;
		}

		System.out.println("Number of refs: " + number_of_refs);
		System.out.println("Num opps: " + opps.size());
		for (String ss : opps) {
			System.out.println(ss);
		}

		// Toolkit.getDefaultToolkit().beep();
		// promptEnterKey(); //***DEBUG
		System.out.println("moving on..."); // ***DEBUG
		System.out.println("***DEBUG RefsNeeded   --->   " + needsRef);
		System.out.println("***DEBUG skippedRefs   --->   " + skippedRefs);
		System.out.println("***DEBUG dbA   --->   " + dbA + " ***");
		System.out.println("***DEBUG dbB   --->   " + dbB + " ***");
		System.out.println("***DEBUG beforeRef   --->   " + beforeRef);

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

	private String getMeCorrectNameFormat(String oldName) {
		String retName = oldName.replace("\\", "/");
		retName = retName.replace("\\", "/");

		File file = new File(projectDirectoryPath);
		String simpleFileName = file.getName();
		String baseDirectory = projectDirectoryPath;// .replace(projectDirectoryPath, simpleFileName);
		baseDirectory = baseDirectory.replace("\\", "/");
		baseDirectory = baseDirectory.replace(simpleFileName, "");

		retName = retName.replaceFirst(baseDirectory, "");

		// TEST <
		// System.out.println("projectDirectoryPath: "+this.projectDirectoryPath);
		// System.out.println("File.separator: "+File.separator);
		// System.out.println("simpleFileName: "+simpleFileName);
		// System.out.println("oldName: "+oldName);
		// System.out.println("retName: "+retName);
		// Toolkit.getDefaultToolkit().beep();
		// promptEnterKey(); //***DEBUG
		// TEST >

		return retName;
	}

	public void promptEnterKey() {
		System.out.println("Press \"ENTER\" to continue...");
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
	}

	MethodOppExtractorSettings settings = null;

	ArrayList<String> opps = new ArrayList<String>();

	int needsRef = 0;
	int skippedRefs = 0;
	int dbA = 0;
	int dbB = 0;
	int beforeRef = 0;

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
					needsRef++;

					if (clazz.getMethods().get(index).getMetricIndexFromName("size") < 50) {// skip methods with less
																							// line
																							// // of code
						continue;
					}
					String className = file.getName().replaceFirst("./", "");
					dbA++;
					String classPath = getMeCorrectNameFormat(file.getAbsolutePath());
					dbB++;
					String methodName = clazz.getMethods().get(index).getName();
					settings = new MethodOppExtractorSettings();
					MethodOppExtractor extractor = new MethodOppExtractor(file, clazz.getMethods().get(index).getName(),
							settings, clazz);

					Method method = clazz.getMethods().getMethodByName(methodName);
					ArrayList<Opportunity> opportunities = method.getOpportunityList().getOptimals();

					beforeRef++;

					int count = 1;
					for (Opportunity opp : opportunities) {
						skippedRefs++;
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

		// File fileDel = new File("./" + file.getName() + "_parsed.txt");
		// fileDel.delete();
		// >
		// ***************************************************************************************************
		return ret;
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

	private boolean doAnalysis_Java(String directoryName) {
		File directory = new File(directoryName);
		if (!directory.exists() || !directory.isDirectory()) {
			return false;
		}
		return getFilesForAnalysis_Java(projectDirectoryPath);
	}

	private boolean getFilesForAnalysis_Java(String directoryPath) {

		boolean ret = true;

		File directory = new File(directoryPath);

		// Get all files from a directory.
		File[] fList = directory.listFiles();
		if (fList != null) {
			for (File file : fList) {
				if (file.isFile() && file.getName().contains(".") && file.getName().charAt(0) != '.') {
					String[] str = file.getName().split("\\.");
					// For all the filles of this dirrecory get the extension
					if (str[str.length - 1].equalsIgnoreCase("java")) {
						java_source_files.add(file);

						ClassParser parser = new ClassParser(file.getAbsolutePath());
						// try {
						// parser.parse();
						// }catch(Exception e) {
						// System.out.println("Exception during parse");
						// parser.parse();
						// }
						parser.parse();
						utils.Utilities.writeCSV("./" + file.getName() + "_original_parsed.txt", parser.getOutput(),
								false);
						utils.Utilities.writeCSV("./" + file.getName() + "_parsed.txt", parser.getOutput(), false);

						// ExtraParseUtils asd = new ExtraParseUtils();
						// asd.convertSimpleIfsToLine("./"+file.getName()+"_parsed.txt");

						// ***************************************************************************************************
						// <
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
						// >
						// ***************************************************************************************************
					}
				} else if (file.isDirectory()) {
					ret = ret && getFilesForAnalysis_Java(file.getAbsolutePath());
				}
			}
		}
		return ret;
	}

	private boolean doAnalysis_C_Cpp(String directoryName) {
		File directory = new File(directoryName);
		if (!directory.exists() || !directory.isDirectory()) {
			return false;
		}
		return getFilesForAnalysis_C(projectDirectoryPath);
	}

	private boolean getFilesForAnalysis_C(String directoryName) {
		System.out.println("directory name = " + directoryName);
		boolean ret = true;
		File directory = new File(directoryName);
		// Get all files from a directory.
		File[] fList = directory.listFiles();
		if (fList != null) {
			for (File file : fList) {
				if (file.isFile() && file.getName().contains(".") && file.getName().charAt(0) != '.') {

					System.out.println("********* file is Source file");
					try {
						String[] str = file.getName().split("\\.");
						// For all the filles of this dirrecory get the extension

						if ((str[str.length - 1].equalsIgnoreCase("c")) || (str[str.length - 1].equalsIgnoreCase("cpp"))
								|| (str[str.length - 1].equalsIgnoreCase("cc"))
								|| (str[str.length - 1].equalsIgnoreCase("cp"))
								|| (str[str.length - 1].equalsIgnoreCase("cxx"))
								|| (str[str.length - 1].equalsIgnoreCase("c++"))
								|| (str[str.length - 1].equalsIgnoreCase("cu"))) {

							CodeFile tempFile = new cFile(file);
							// new cFileNew(file);

							System.out.println("***DEBUG 'c' Parsing: " + file.getName());

							tempFile.parse();

							// ***TEST ***DEBUG < allazei to arxeio parsed.txt kai enonei ta "mikra" IF
							// //***POINT TEST_COM

							// ExtraParseUtils asd = new ExtraParseUtils();
							// asd.convertSimpleIfsToLine("./"+file.getName()+"_parsed.txt");
							// ***TEST ***DEBUG >

							// ***************************************************************************************************
							// <
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

							// >
							// ***************************************************************************************************
						}
					} catch (OutOfMemoryError E) {
						System.out.println("Out of Memory for file: " + file.getAbsolutePath());
						skippedFiles.add(file.getAbsolutePath());
					}
				} else if (file.isDirectory()) {
					System.out.println("********* file is folder");
					ret = ret && getFilesForAnalysis_C(file.getAbsolutePath());
				}
			}
		}

		return ret;
	}

}
