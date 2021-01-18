package test;

import java.io.File;

import jarExecuteClass.BasicController;
import utils.ExtraParseUtils;

public class mainTest {

	public static void main(String[] args) {

		doEndStuff();

		basicTestThings();


	}

	private static void doEndStuff() {
		File folder = new File(System.getProperty("user.dir"));
		File fileList[] = folder.listFiles();
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].getName().endsWith("_parsed.txt")) {
				fileList[i].delete();
			}
		}
	}

	public static void basicTestThings() {
		long start = System.currentTimeMillis();
		long end;

		String prjPath = "";
		String dbPath = "noDB";
		
		prjPath = "C:\\Users\\temp\\Downloads\\rodinia-master";
//		prjPath = "C:\\Users\\temp\\Downloads\\kameleon2";
		prjPath = "C:\\Users\\temp\\Documents\\GitHub\\Workspace\\UoM\\SDK4ED\\_Partners\\Neurasmus8\\imd-emulator";
		BasicController ctrl = new BasicController("cpp", "TestPrj", "0", prjPath, dbPath);

		
//		prjPath = "C:\\Users\\temp\\Downloads\\jcommander-main";
//		prjPath = "C:\\Users\\temp\\Documents\\GitHub\\DeRec-GEA";
//		BasicController ctrl = new BasicController("java", "TestPrj", "0", prjPath, dbPath);

		boolean result = ctrl.runExperiment(); // ***TEMP_COMMENT

		doEndStuff(); // ***POINT TEST_COM diagrfei ta _parsed arxeia.
		System.out.println("Telos to test!!!!");
		if (result) {
			System.out.println("Executed correctly");
		} else {
			System.out.println("There was an error");
		}
		end = System.currentTimeMillis();
		System.out.println("Total Time: " + ((end - start) / 1000) / 60 + " minutes");

	}

}
