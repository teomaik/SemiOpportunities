package jarExecuteClass.parallelSemi;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import AST.ClassParser;

public class ActiveFiles {
//	private ArrayList<String> activeFiles = new ArrayList<String>();
	ArrayList<String> filePaths = new ArrayList<String>();
	ArrayList<Integer> fileStatus = new ArrayList<Integer>();
	

	Lock globalSumLock = new ReentrantLock();

	public void tempFix() {
		
	}

	public void printFilesAndStatus() {
		for(int i=0; i<this.filePaths.size(); i++) {
			System.out.println("File: "+this.filePaths.get(i)+", Status: "+this.fileStatus.get(i));
		}
	}

	int debugFilesStarted = 0;
	int debugFilesOriginal = 0;
	public void addNewFile(String filePath) {
		System.out.println("Inserting: " + filePath);
		this.filePaths.add(filePath);
		this.fileStatus.add(0);
		debugFilesOriginal++;
	}

	public String giveMePathForAnalysis() {
		this.lock();
		
		for(int i=0; i<this.filePaths.size(); i++) {
			if(this.fileStatus.get(i).intValue()!=0) {
				continue;
			}
			File potentialFile = new File(this.filePaths.get(i));
			boolean isFileAvailable = true;
			for(int c=0; c<this.filePaths.size(); c++) {
				File fileCheck = new File(this.filePaths.get(c));
				if(!potentialFile.getName().equals(fileCheck.getName())) {
					continue;
				}
				if(this.fileStatus.get(c).intValue()==1) {
					isFileAvailable = false;
					break;
				}
			}
			if(isFileAvailable) {
				this.fileStatus.set(i, 1);
				debugFilesStarted++;
				this.unlock();
				return this.filePaths.get(i);
			}
		}

		this.unlock();
		//System.out.println("Returning null path");
		return null;
	}

	public boolean areWeDone() {
		this.lock();
		if (fileStatus.size() == 0) {	//TODO
			this.unlock();
			return true;
		}
		//printFilesAndStatus();
		this.unlock();
		return false;
	}

	public void finishedFileAnaysis(String file_path) {
		this.lock();
		for(int i=0; i<this.filePaths.size(); i++) {
			if(this.filePaths.get(i).equals(file_path)) {
				this.filePaths.remove(i);
				this.fileStatus.remove(i);
			}
		}
		System.out.println("Done analyzing: "+file_path);
		this.unlock();
	}

	public void lock() {
		globalSumLock.lock();
	}

	public void unlock() {
		globalSumLock.unlock();
	}
	
	public boolean debugAllGood() {
		System.out.println("Num of files: "+debugFilesOriginal);
		return debugFilesStarted==debugFilesOriginal;
	}

	
//	Lock parseLock = new ReentrantLock();
	ClassParser parser;
	synchronized void parseDebug(File file) {
		parser = new ClassParser(file.getAbsolutePath());

		parser.parse();
		//utils.Utilities.writeCSV("./" + file.getName() + "_original_parsed.txt", parser.getOutput(), false);
		utils.Utilities.writeCSV("./" + file.getName() + "_parsed.txt", parser.getOutput(), false);
	}

	public ArrayList<String> getFilePaths() {
		return filePaths;
	}
}
