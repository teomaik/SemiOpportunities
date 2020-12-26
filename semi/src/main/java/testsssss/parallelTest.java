package testsssss;

import java.io.File;

import jarExecuteClass.parallelSemi.ActiveFiles;

public class parallelTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ActiveFiles act = new ActiveFiles();
		act.putNewFile("C:\\Users\\temp\\Downloads\\semiParallTest\\inner\\f1.txt");
		act.putNewFile("C:\\Users\\temp\\Downloads\\semiParallTest\\inner\\f2.txt");
		act.putNewFile("C:\\Users\\temp\\Downloads\\semiParallTest\\inner\\f3.txt");

		act.putNewFile("C:\\Users\\temp\\Downloads\\semiParallTest\\f1.txt");
		act.putNewFile("C:\\Users\\temp\\Downloads\\semiParallTest\\f2.txt");
		act.putNewFile("C:\\Users\\temp\\Downloads\\semiParallTest\\f3.txt");
		act.putNewFile("C:\\Users\\temp\\Downloads\\semiParallTest\\f4.txt");
		act.printFilesAndStatus();
		System.out.println("Done: "+act.areWeDone());
		
		for(int i=0; i<4; i++) {
			System.out.println("i: "+i);
			String path = act.giveMePathForAnalysis();
			if(path!=null) {
				System.out.println(path);
				File f = new File(path);
			}else {
				System.out.println("null path");
			}
		}
		
		act.finishedFileAnaysis("C:\\Users\\temp\\Downloads\\semiParallTest\\inner\\f2.txt");
		System.out.println("New file for analysis: "+act.giveMePathForAnalysis());
		System.out.println("Done: "+act.areWeDone());

		act.finishedFileAnaysis("C:\\Users\\temp\\Downloads\\semiParallTest\\inner\\f1.txt");
		act.finishedFileAnaysis("C:\\Users\\temp\\Downloads\\semiParallTest\\inner\\f3.txt");
		act.finishedFileAnaysis("C:\\Users\\temp\\Downloads\\semiParallTest\\f1.txt");
		act.finishedFileAnaysis("C:\\Users\\temp\\Downloads\\semiParallTest\\f2.txt");
		act.finishedFileAnaysis("C:\\Users\\temp\\Downloads\\semiParallTest\\f3.txt");
		act.finishedFileAnaysis("C:\\Users\\temp\\Downloads\\semiParallTest\\f4.txt");

		System.out.println("Done: "+act.areWeDone());
	}

}
