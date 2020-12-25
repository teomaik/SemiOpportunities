package db;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.mysql.cj.jdbc.MysqlDataSource;

public class DbController {

	Lock globalInsertLock = new ReentrantLock();
	
	String path = "";
	Connection conn = null;
	MysqlDataSource dataSource = null;
	String username = null;
	String password = null;
	String serverName = null;
	String databaseName = null;

	public int debCounter = 0;
	public int insertCounter = 0;
	public int updateCounter = 0;

	ArrayList<PreparedStatement> stmts = new ArrayList<PreparedStatement>();
	ArrayList<String> projectName = new ArrayList<String>();
	ArrayList<String> className = new ArrayList<String>();
	ArrayList<String> methodName = new ArrayList<String>();
	ArrayList<String> classPath = new ArrayList<String>();

	ArrayList<Integer> line_start = new ArrayList<Integer>();
	ArrayList<Integer> line_end = new ArrayList<Integer>();

	ArrayList<Double> cohesion_benefit = new ArrayList<Double>();
	ArrayList<Double> methodOriginalCohesion = new ArrayList<Double>();
	ArrayList<Double> LoC = new ArrayList<Double>();

	public DbController(String path) {
		this.path = path;
		conn = getConnection(path);
		if (!this.isReady()) {
			System.out.println("Null Connection");
		} else {
			boolean ok = beginRUTransaction();
			if (!ok) {
				this.closeConn();
			}
		}
	}

	public boolean isReady() {
		if (conn != null) {
			return true;
		}
		return false;
	}

	public boolean dbActions(String projectName, int C_ProjectVersion) {
		if (projectName == null || projectName.isEmpty() || C_ProjectVersion < 0) {
			return false;
		}
		if (!this.isReady()) {
			return false;
		}
		boolean ret;
		setVersion(C_ProjectVersion);
		ret = deletePreviousInsertsOfProject(projectName, C_ProjectVersion);
		return ret && doInserts(C_ProjectVersion);
	}

	private boolean doInserts(int C_ProjectVersion) {
		if (!this.isReady()) {
			return false;
		}
		try {
			for (int i = 0; i < this.className.size(); i++) {
				String prName = projectName.get(i);
				String clsName = className.get(i);
				String mthName = methodName.get(i);
				String clsPath = classPath.get(i);
				int lnStart = line_start.get(i);
				int lnEnd = line_end.get(i);
				double cohBen = cohesion_benefit.get(i);
				double orgCoh = methodOriginalCohesion.get(i);
				double loc = LoC.get(i);

				// String query = " insert into OPPORTUNITIES (PROJECT_NAME, CLASS_NAME,
				// METHOD_NAME, LINE_START, LINE_END, COHESION_BENEFIIT,
				// METHOD_ORIGINAL_COHESION, LINES_OF_CODE, CLASS_PATH)"
				// + " values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

				String query = " insert into OPPORTUNITIES (PROJECT_NAME, CLASS_NAME, METHOD_NAME, LINE_START, LINE_END, COHESION_BENEFIIT, METHOD_ORIGINAL_COHESION, LINES_OF_CODE, CLASS_PATH, PROJECT_VERSION)"
						+ " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

				// create the mysql insert preparedstatement
				PreparedStatement preparedStmt = conn.prepareStatement(query);
				preparedStmt.setString(1, prName);
				preparedStmt.setString(2, clsName);
				preparedStmt.setString(3, mthName);
				preparedStmt.setInt(4, lnStart);
				preparedStmt.setInt(5, lnEnd);
				preparedStmt.setDouble(6, cohBen);
				preparedStmt.setDouble(7, orgCoh);
				preparedStmt.setDouble(8, loc);
				preparedStmt.setString(9, clsPath);
				preparedStmt.setInt(10, C_ProjectVersion);

				int res = preparedStmt.executeUpdate();
				insertCounter++;
				if (res <= 0) {
					this.connRollBackAndClose();
					return false;
				}
			}
		} catch (Exception e) {
			System.out.println("Could not execute INSERT to Database");
			System.out.println(e.getMessage());
		}

		return true;
	}

	public void closeConn() {
		try {
			this.conn.close();
			this.conn = null;
		} catch (Exception e) {
			this.connRollBackAndClose();
		}

	}

	public boolean insertMethodToDatabase(String projectName, String className, String methodName, int line_start,
			int line_end, double cohesion_benefit, double methodOriginalCohesion, double LoC, String classPath) {

		if (projectName == null || projectName.isEmpty() || className == null || className.isEmpty()
				|| methodName == null || methodName.isEmpty() || classPath == null || classPath.isEmpty()
				|| cohesion_benefit < 0 || line_start < 0 || line_end < 0 || line_start >= line_end || LoC <= 0) {
			return false;
		}
		this.lock();
		debCounter++;

		this.projectName.add(projectName);
		this.className.add(className);
		this.methodName.add(methodName);
		this.classPath.add(classPath);
		this.line_start.add(line_start);
		this.line_end.add(line_end);
		this.cohesion_benefit.add(cohesion_benefit);
		this.methodOriginalCohesion.add(methodOriginalCohesion);
		this.LoC.add(LoC);
		this.unlock();
		return true;
	}

	private int version;
	String projName;

	private boolean deletePreviousInsertsOfProject(String projectName, int projectVersion) {
		if (!this.isReady()) {
			return false;
		}
		try {
			String sql = "DELETE FROM OPPORTUNITIES WHERE PROJECT_NAME=? AND PROJECT_VERSION=?";
			PreparedStatement prpStmt = conn.prepareStatement(sql);
			prpStmt.setString(1, projectName);
			prpStmt.setInt(2, projectVersion);
			prpStmt.executeUpdate();
			return true;

		} catch (SQLException e) {
			System.out.println("Could not DELETE old database info for this project");
			e.printStackTrace();
			System.out.println(e.getMessage());
			return false;
		}
	}

	private boolean beginRUTransaction() { // READ_UNCOMMITTED_SQL_TRANSACTION
		if (!this.isReady()) {
			return false;
		}
		try {
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(conn.TRANSACTION_READ_UNCOMMITTED);
			return true;
		} catch (Exception e) {
			System.out.println("Could not start a Read_Unncommitted transaction");
			return false;
		}
	}

	public boolean connRollBackAndClose() {
		if (!this.isReady()) {
			return false;
		}
		try {
			conn.rollback();
			conn.close();

			System.out.println("Rolling back transaction");
			return true;
		} catch (SQLException exc) {
			System.out.println("Could not roll back transaction");
			exc.printStackTrace();
			return false;
		}
	}

	public boolean connCommitAndClose() {
		if (!this.isReady()) {
			return false;
		}
		try {
			conn.commit();
			conn.close();

			System.out.println("Commiting transaction");
			return true;
		} catch (SQLException exc) {
			System.out.println("Could not commit transaction");
			exc.printStackTrace();
			connRollBackAndClose();
			return false;
		}
	}

	public void getNewConnection(String path) {
		this.conn = getConnection(path);
		beginRUTransaction();
	}

	private Connection getConnection(String path) {

		if (path == null || path.isEmpty()) {
			return null;
		}

		boolean ok = false;

		try {
			ok = true;
			// BufferedReader reader = new BufferedReader(new FileReader(filename));
			// String line;

			File file = new File(path);

			if (!file.exists() || !file.isFile()) {
				return null;
			}

			Scanner input = new Scanner(new FileInputStream(file));

			boolean flag = input.hasNextLine();
			if (!input.hasNextLine()) {
				input.close();
				return null;
			}

			while (flag) {
				String line = input.nextLine();
				if (line.startsWith("username=")) {
					username = line.replaceFirst("username=", "");
					// username = line;
				} else if (line.startsWith("password=")) {
					password = line.replaceFirst("password=", "");
					// password = line;
				} else if (line.startsWith("serverName=")) {
					serverName = line.replaceFirst("serverName=", "");
					// serverName = line;
				} else if (line.startsWith("databaseName=")) {
					databaseName = line.replaceFirst("databaseName=", "");
					// databaseName = line;
					// "jdbc:mysql://"+serverName+"/"+line+ "?user=" +username + "&password=" +
					// password + "&useUnicode=true&characterEncoding=UTF-8";
				}
				flag = input.hasNextLine();
			}
			input.close();
			// if (username == null || password == null || serverName == null ||
			// databaseName == null) {
			if (serverName == null || databaseName == null) {

				ok = false;
			}
			if (!ok) {
				System.out.println("One or more of the Credentials given is null");
				return null;
			}

		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", path);
			e.printStackTrace();
			return null;
		}

		// <

		String url = "jdbc:mysql://" + serverName + "/" + databaseName + "";

		System.out.println("Connecting database...");

		try {
			Connection connection = DriverManager.getConnection(url, username, password);
			System.out.println("Database connected!");
			return connection;
		} catch (SQLException e) {
			System.out.println("Cannot connect the database!\n" + e.getMessage());
			return null;
		}
		// >

	}
	public void lock() {
		this.globalInsertLock.lock();
	}
	public void unlock() {
		this.globalInsertLock.unlock();
	}
	public int getVersion() {
		return version;
	}

	private void setVersion(int version) {
		this.version = version;
	}

}