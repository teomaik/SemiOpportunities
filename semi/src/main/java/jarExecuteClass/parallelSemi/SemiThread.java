package jarExecuteClass.parallelSemi;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

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
import utils.Utilities;

public class SemiThread extends Thread {

	private ActiveFiles filesForAnalysis;
	private String selected_metric;
	private String progrLang;
	private ArrayList<String> opps;
	private DbController dbCon;
	private String projectName;
	private ArrayList<String> skippedFiles;
	private String projectDirectoryPath;

	private boolean success = true;
	private MethodOppExtractorSettings settings = null;

	public SemiThread(ActiveFiles filesForAnalysis, String selected_metric, String progrLang, ArrayList<String> opps,
			DbController dbCon, String projectName, ArrayList<String> skippedFiles, String projectDirectoryPath) {
		super();
		this.filesForAnalysis = filesForAnalysis;
		this.selected_metric = selected_metric;
		this.progrLang = progrLang;
		this.opps = opps;
		this.dbCon = dbCon;
		this.projectName = projectName;
		this.skippedFiles = skippedFiles;
		this.projectDirectoryPath = projectDirectoryPath;

		this.start();
	}

	public void run() {
		while (!filesForAnalysis.areWeDone()) {
			String filePathForAnalysis = filesForAnalysis.giveMePathForAnalysis();
			if (filePathForAnalysis == null) {
				continue;
			}
			filesForAnalysis.finishedFileAnaysis(filePathForAnalysis);
			success = success && doParallelAnalysis(filePathForAnalysis);
		}
	}

	private ArrayList<String> alayzedMethods = new ArrayList<String>();
	private ArrayList<String> methodsForAnalysis = new ArrayList<String>();
	ClassParser parser;
	Utilities writer = new Utilities();

	private ArrayList<String> localopps = new ArrayList<String>();

	private boolean doParallelAnalysis(String filePath) {
		boolean ret = true;

		File file = new File(filePath);

		if (progrLang.equals("java")) {

//			synchronized(parser = new ClassParser(file.getAbsolutePath())){
//
			synchronized (this.getClass()) {
				this.parser = new ClassParser(file.getAbsolutePath());
				parser.parse();
				utils.Utilities.writeCSV("./" + file.getName() + "_parsed.txt", parser.getOutput(), false);
//				synchronized(writer){
//					writer.writeCSV("./" + file.getName() + "_parsed.txt", parser.getOutput(), false);
//			      }
			}

//			filesForAnalysis.parseDebug(file);

//			return false;

//			ClassParser parser = new ClassParser(file.getAbsolutePath());
//			parser.parse();
//			utils.Utilities.writeCSV("./" + file.getName() + "_parsed.txt", parser.getOutput(), false);

		} else if (progrLang.equals("c") || progrLang.equals("cpp")) {
			CodeFile tempFile = new cFile(file);
			tempFile.parse();
		} else {
			return false;
		}

		Analyser analyser = new Analyser();
		analyser.setFile(file);

		try {

			JavaClass clazz = analyser.performAnalysis();

			for (int index = 0; index < clazz.getMethods().size(); index++) {
				boolean needsRefactoring = clazz.getMethods().get(index).needsRefactoring(selected_metric);

				if (needsRefactoring) {

					// skip methods with less line of code
					if (clazz.getMethods().get(index).getMetricIndexFromName("size") < 50) {
						continue;
					}

					alayzedMethods.add(filePath + "/" + clazz.getMethods().get(index).getName() + "");
					String className = file.getName().replaceFirst("./", "");
					String classPath = getMeCorrectNameFormat(file.getAbsolutePath());
					String methodName = clazz.getMethods().get(index).getName();
					settings = new MethodOppExtractorSettings();
					MethodOppExtractor extractor = new MethodOppExtractor(file, clazz.getMethods().get(index).getName(),
							settings, clazz);

					Method method = clazz.getMethods().getMethodByName(methodName);
					ArrayList<Opportunity> opportunities = method.getOpportunityList().getOptimals();

					if (opportunities.size() > 0) {
						Opportunity opp = opportunities.get(0);
						localopps.add(className + "." + methodName + " -> " + opp.getStartLineCluster() + "-"
								+ opp.getEndLineCluster() + " : " + opp.getOpportunityBenefitMetricByName("lcom2"));
						opps.add(className + "." + methodName + " -> " + opp.getStartLineCluster() + "-"
								+ opp.getEndLineCluster() + " : " + opp.getOpportunityBenefitMetricByName("lcom2"));
						ret = ret && dbCon.insertMethodToDatabase(projectName, className, methodName,
								opp.getStartLineCluster(), opp.getEndLineCluster(),
								opp.getOpportunityBenefitMetricByName("lcom2"), method.getMetricIndexFromName("lcom2"),
								method.getMetricIndexFromName("size"), classPath);
					}

					// alayzedFaliedMethods.remove(alayzedFaliedMethods.size()-1);
				}
			}

		} catch (OutOfMemoryError E) {
			skippedFiles.add(file.getAbsolutePath());
			alayzedMethods.add("at try catch");
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

	public boolean isSuccessful() {
		for (String s : localopps) {
			System.out.println("opp_loc: " + s);
		}
		return success;
	}

	public void debug() {
//		for(String s : alayzedMethods) {
//			System.out.println("Methods analyzed: "+s);
//		}
	}

}
