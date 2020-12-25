package jarExecuteClass.parallelSemi;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ActiveFiles {
//	private ArrayList<String> activeFiles = new ArrayList<String>();
	private HashMap<String, Integer> fileStatus = new HashMap<String, Integer>(); // < File_Path, file_status> 0
																					// available, 1 running, 2 done

	Lock globalSumLock = new ReentrantLock();

	public void printFilesAndStatus() {
		Iterator it = fileStatus.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			// System.out.println(pair.getKey() + " = " + (ClassMetrics)pair.getValue());
			Integer cm = this.fileStatus.get(pair.getKey());

			System.out.println("Class : " + pair.getKey()+", Status: "+cm.intValue());

			it.remove(); // avoids a ConcurrentModificationException
		}
	}

	public void putNewFile(String filePath) {
		System.out.println("Inserting: " + filePath);
		this.fileStatus.put(filePath, 0);
	}

	public String giveMePathForAnalysis() {
		this.lock();
		Iterator availableIteratot = fileStatus.entrySet().iterator();
		while (availableIteratot.hasNext()) {
			Map.Entry av_pair = (Map.Entry) availableIteratot.next();

			File potential_file = new File((String) av_pair.getKey());
			Integer status = this.fileStatus.get(av_pair.getKey());

			Iterator it = fileStatus.entrySet().iterator();
			boolean good_to_go = true;
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();

				File file = new File((String) pair.getKey());
				if (!file.getName().equals(potential_file.getName())) {
					it.remove();
					continue;
				}

				Integer status_2 = this.fileStatus.get(pair.getKey());
				if (status_2.intValue() == 1) {
					good_to_go = false;
				}
			}

			if (good_to_go) {
				fileStatus.put((String) av_pair.getKey(), 1);
				this.unlock();
				return (String) av_pair.getKey();
			}

			availableIteratot.remove(); // avoids a ConcurrentModificationException
		}

		this.unlock();
		return null;
	}

	public boolean areWeDone() {
		this.lock();
		if (fileStatus.size() == 0) {
			this.unlock();
			return true;
		}

		this.unlock();
		return false;
	}

	public void finishedFileAnaysis(String file_path) {
		this.lock();
		fileStatus.remove(file_path);
		this.unlock();
	}

	public void lock() {
		globalSumLock.lock();
	}

	public void unlock() {
		globalSumLock.unlock();
	}
}
