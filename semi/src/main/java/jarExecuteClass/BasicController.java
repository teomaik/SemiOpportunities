package jarExecuteClass;

import java.io.File;
import java.util.ArrayList;

import AST.ClassParser;
import clustering.cluster.Opportunity;
import db.DbController;
import gui.Analyser;
import gui.MethodOppExtractor;
import gui.MethodOppExtractorSettings;
import parsers.CodeFile;
import parsers.cFile;
import parsers.cFileNew;
import parsers.fortranFile;
import splitlongmethod.JavaClass;
import splitlongmethod.Method;

public class BasicController {

	DbController dbCon = null;

	private String projectName = null;
	private String projectProgramingLanguage = null;
	private String projectDirectoryPath = null;
	private String credPath = null;

	private ArrayList<JavaClass> classResults = new ArrayList<>();
	private String selected_metric = "SIZE";// "LCOM1", "LCOM2", "LCOM4", "COH", "CC" //***???

	private int number_of_refs = 0;

	// Fortran, Fortran90, C, Cpp projects
	private ArrayList<CodeFile> projectFiles = new ArrayList<>();
	private ArrayList<CodeFile> cHeaderFiles = new ArrayList<>();

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

		if (this.dbCon == null || !this.dbCon.isReady()) {
			System.out.println("Problem with databaseConnection");
			return false; // ***POINT TEST_COM
		}
		
		dbCon.closeConn();

		boolean commit = true;

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
		case "f":
			commit = doAnalysis_F(projectDirectoryPath);
			break;
		case "f90":
			commit = doAnalysis_F(projectDirectoryPath);
			break;

		default:
			System.out.println("wrong argument for Programing Language (java, c, f, f90)");
		}

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
		String retName = oldName;
		String[] splited = projectDirectoryPath.split(File.separator);
		String baseDirectory = splited[splited.length - 1];
		retName = retName.replaceFirst(projectDirectoryPath, baseDirectory);

		return retName;
	}

	MethodOppExtractorSettings settings = null;

	ArrayList<String> opps = new ArrayList<String>();

	private boolean doAnalysis(File file) {
		// ***************************************************************************************************
		// <
		boolean ret = true;

		analyser.setFile(file);
		//System.out.println("***DEBUG NAME:" + file.getName());
		classResults.add(analyser.performAnalysis());

		JavaClass clazz = classResults.get(classResults.size() - 1);

		for (int index = 0; index < clazz.getMethods().size(); index++) {
			boolean needsRefactoring = clazz.getMethods().get(index).needsRefactoring(selected_metric);

			if (needsRefactoring) {
				if (clazz.getMethods().get(index).getMetricIndexFromName("size") < 50) {// skip methods with less line
																						// // of code
					continue;
				}
				String className = file.getName().replaceFirst("./", "");
				String classPath = getMeCorrectNameFormat(file.getAbsolutePath());
				String methodName = clazz.getMethods().get(index).getName();
				settings = new MethodOppExtractorSettings();
				MethodOppExtractor extractor = new MethodOppExtractor(file, clazz.getMethods().get(index).getName(),
						settings, classResults.get(classResults.size() - 1));

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
					count++;
				}
			}
		}
		//File fileDel = new File("./" + file.getName() + "_parsed.txt");
		// fileDel.delete();
		// >
		// ***************************************************************************************************
		return ret;
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
						utils.Utilities.writeCSV("./" + file.getName() + "_parsed.txt", parser.getOutput(), false);
						// ***************************************************************************************************
						// <
						try {
							ret = ret && doAnalysis(file);
						} catch (Exception e) {
							System.out.println("File analysis failed, " + e.getMessage());
							try {
								File fileDel = new File("./" + file.getName() + "_parsed.txt");
								fileDel.delete();	//***POINT TEST_COM
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
					String[] str = file.getName().split("\\.");
					// For all the filles of this dirrecory get the extension

					if ((str[str.length - 1].equalsIgnoreCase("c")) || (str[str.length - 1].equalsIgnoreCase("cpp"))
							|| (str[str.length - 1].equalsIgnoreCase("cc"))
							|| (str[str.length - 1].equalsIgnoreCase("cp"))
							|| (str[str.length - 1].equalsIgnoreCase("cxx"))
							|| (str[str.length - 1].equalsIgnoreCase("c++"))
							|| (str[str.length - 1].equalsIgnoreCase("cu"))) {

						projectFiles.add(new cFile(file));
						new cFileNew(file);

						System.out.println("***DEBUG 'c' Parsing: " + file.getName());

						projectFiles.get(projectFiles.size() - 1).parse();
						// ***************************************************************************************************
						// <
						try {
							ret = ret && doAnalysis(file);
						} catch (Exception e) {
							System.out.println("File analysis failed, " + e.getMessage());
							try {
								File fileDel = new File("./" + file.getName() + "_parsed.txt");
								fileDel.delete();	//***POINT TEST_COM
							} catch (Exception exc) {
								System.out.println("Exception while deleting file");
							}
						}

						// >
						// ***************************************************************************************************
					} else if ((str[str.length - 1].equalsIgnoreCase("h"))
							|| (str[str.length - 1].equalsIgnoreCase("hpp"))
							|| (str[str.length - 1].equalsIgnoreCase("hh"))
							|| (str[str.length - 1].equalsIgnoreCase("hp"))
							|| (str[str.length - 1].equalsIgnoreCase("hxx"))
							|| (str[str.length - 1].equalsIgnoreCase("h++"))
							|| (str[str.length - 1].equalsIgnoreCase("hcu"))) {

						cHeaderFiles.add(new cFile(file));
						cHeaderFiles.get(cHeaderFiles.size() - 1).parse();
					}
				} else if (file.isDirectory()) {
					ret = ret && getFilesForAnalysis_C(file.getAbsolutePath());
				}
			}
		}

		return ret;
	}

	private boolean doAnalysis_F(String directoryName) {
		File directory = new File(directoryName);
		if (!directory.exists() || !directory.isDirectory()) {
			return false;
		}
		return getFilesForAnalysis_F(projectDirectoryPath);
	}

	public boolean getFilesForAnalysis_F(String directoryName) {
		System.out.println("directory name = " + directoryName);
		boolean ret = true;
		File directory = new File(directoryName);
		// Get all files from a directory.
		File[] fList = directory.listFiles();
		if (fList != null) {
			for (File file : fList) {
				if (file.isFile() && file.getName().contains(".") && file.getName().charAt(0) != '.') {
					String[] str = file.getName().split("\\.");
					// For all the filles of this dirrecory get the extension
					if (str[str.length - 1].equalsIgnoreCase("F90")) {

						projectFiles.add(new fortranFile(file, true));
						projectFiles.get(projectFiles.size() - 1).parse();
						// ***************************************************************************************************
						// <
						ret = ret && doAnalysis(file);
						// >
						// ***************************************************************************************************

					} else if (str[str.length - 1].equalsIgnoreCase("f") || str[str.length - 1].equalsIgnoreCase("f77")
							|| str[str.length - 1].equalsIgnoreCase("for")
							|| str[str.length - 1].equalsIgnoreCase("fpp")
							|| str[str.length - 1].equalsIgnoreCase("ftn")) {

						projectFiles.add(new fortranFile(file, false));
						projectFiles.get(projectFiles.size() - 1).parse();
						// ***************************************************************************************************
						// <
						ret = ret && doAnalysis(file);
						// >
						// ***************************************************************************************************

					}
				} else if (file.isDirectory()) {
					ret = ret && getFilesForAnalysis_C(file.getAbsolutePath());
				}
			}
		}

		return ret;
	}
}
