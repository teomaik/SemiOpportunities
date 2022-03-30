package jarExecuteClass;

import java.io.File;

import utils.ExtraParseUtils;

public class mainOpps {

	public static void main(String[] args) {

		doEndStuff();
		
		if (args.length != 8) {
			System.out.println("Wrong number of arguments");
			System.out.println("You need to provide 5 arguments: " 
			+ "\n1: programming language (java, c, cpp)"// , f,																												// f90)"
			+ "\n2: project name" 
			+ "\n3: project version" 
			+ "\n4: path to project directory"
			+ "\n5: The server url for the database"
			+ "\n6: The database name"
			+ "\n7: The username for the database"
			+ "\n8: The password for the database");
			System.exit(1);
		}

		if (!args[0].equals("c") && !args[0].equals("cpp") && !args[0].equals("java")) {
			System.out.println("Wrong programming language, only accepts: c / cpp / java");

			System.exit(1);
			return;
		}

		long start = System.currentTimeMillis();
		long end;

		//serverName, databaseName, username, password
		BasicController controller = new BasicController(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
		boolean result = controller.runExperiment();

		doEndStuff();

		if (result) {
			System.out.println("Executed correctly");
			end = System.currentTimeMillis();
			System.out.println("Total Time: " + ((end - start) / 1000) / 60 + " minutes");
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
