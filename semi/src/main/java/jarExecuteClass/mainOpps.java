package jarExecuteClass;

import java.io.File;

import utils.ExtraParseUtils;

public class mainOpps {

	public static void main(String[] args) {
		//doEndStuff();
		
		long start = System.currentTimeMillis();
		long end;
		
		boolean test = false;
		//test = true;	//***POINT TEST_COM
		if(test) {
			
			boolean utilTest = false;
			if(utilTest) {
				ExtraParseUtils asd = new ExtraParseUtils();
				asd.convertSimpleIfsToLine("./Axis.java_parsed.txt");
				return;
			}
			
			
			///home/anon/Downloads/testNikos
			//BasicController controller = new BasicController("java", "test", "222", "/home/anon/Downloads/testNikos/_testFolder", "nosd");
			BasicController controller = new BasicController("cpp", "test", "222", "/home/anon/Downloads/testNikos/_testFolder", "nosd");
			
			boolean result = controller.runExperiment();

			//doEndStuff();	//***POINT TEST_COM
System.out.println("Telos to test!!!!");
			if (result) {
				System.out.println("Executed correctly");
			} else {
				System.out.println("There was an error");
			}
			end = System.currentTimeMillis();
			System.out.println("Total Time: "+((end-start)/1000)/60+" minutes");
			return;
		}
		
		if (args.length != 5) {
			System.out.println("Wrong number of arguments");
			System.out.println("You need to provide 5 arguments: " + "\n1: programming language (java, c, cpp)"//, f, f90)"
					+ "\n2: project name" + "\n3: project version" + "\n4: path to project directory"
					+ "\n5: path to database credential file");
			System.exit(1);
		}

		if (!args[0].equals("c") && !args[0].equals("cpp") && !args[0].equals("java")) {
			System.out.println("Wrong programming language, only accepts: c / cpp / java");

			System.exit(1);
			return;
		}

		BasicController controller = new BasicController(args[0], args[1], args[2], args[3], args[4]);
		boolean result = controller.runExperiment();

		doEndStuff();

		if (result) {
			System.out.println("Executed correctly");
			System.exit(0);
		} else {
			System.out.println("There was an error");
			System.exit(1);
		}

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

}
