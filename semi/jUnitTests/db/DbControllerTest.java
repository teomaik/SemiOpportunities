package db;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DbControllerTest {

	@Test
	void testDbController() {
		DbController db = new DbController("path/to/creadentials/file.txt");
		db = new DbController(null);
		db = new DbController("");
		db = new DbController("   ");
	}

	@Test
	void testIsReady() {
		DbController db = new DbController("path/to/creadentials/file.txt");
		assertFalse(db.isReady());

		db = new DbController(null);
		assertFalse(db.isReady());

		db = new DbController("");
		assertFalse(db.isReady());

		db = new DbController("   ");
		assertFalse(db.isReady());

	}

	@Test
	void testDbActions() {
		DbController db = new DbController("path/to/creadentials/file.txt");
		assertFalse(db.dbActions("projectName", 8));
		assertFalse(db.dbActions(null, 8));
		assertFalse(db.dbActions("", 8));
		assertFalse(db.dbActions("   ", 8));

		db = new DbController(null);
		assertFalse(db.dbActions("projectName", 8));
		assertFalse(db.dbActions(null, 8));
		assertFalse(db.dbActions("", 8));
		assertFalse(db.dbActions("   ", 8));

		db = new DbController("");
		assertFalse(db.dbActions("projectName", 8));
		assertFalse(db.dbActions(null, 8));
		assertFalse(db.dbActions("", 8));
		assertFalse(db.dbActions("   ", 8));

		db = new DbController("   ");
		assertFalse(db.dbActions("projectName", 8));
		assertFalse(db.dbActions(null, 8));
		assertFalse(db.dbActions("", 8));
		assertFalse(db.dbActions("   ", 8));
	}

	@Test
	void testCloseConn() {
		DbController db = new DbController("path/to/creadentials/file.txt");
		db.closeConn();

		db = new DbController(null);
		db.closeConn();

	}

	@Test
	void testInsertMethodToDatabase() {
		DbController db = new DbController("path/to/creadentials/file.txt");
		assertTrue(db.insertMethodToDatabase("text", "text", "text", 1, 6, 111, 333, 2, "classPath"));

		assertFalse(db.insertMethodToDatabase("text", "text", "text", 1, 1, 111, 333, 2, "classPath"));
		assertFalse(db.insertMethodToDatabase("text", "text", "text", 1, 0, 111, 333, 2, "classPath"));
		assertFalse(db.insertMethodToDatabase("text", "text", "text", -1, 10, 111, 333, 2, "classPath"));
		assertFalse(db.insertMethodToDatabase("text", "text", "text", 1, -10, 111, 333, 2, "classPath"));
		assertFalse(db.insertMethodToDatabase("text", "text", "text", 1, 10, 111, 333, 0, "classPath"));
		assertFalse(db.insertMethodToDatabase("text", "text", "text", 1, 10, 111, 333, -2, "classPath"));
		assertFalse(db.insertMethodToDatabase("text", "text", "text", 1, 10, -111, 333, 2, "classPath"));
		assertFalse(db.insertMethodToDatabase("text", "text", "text", 1, 10, 0, 333, 2, "classPath"));

		assertFalse(db.insertMethodToDatabase(null, "", " ", 1, 6, 111, 333, 2, "classPath"));

		db = new DbController(null);
		assertFalse(db.insertMethodToDatabase(null, "", " ", 1, 6, 111, 333, 2, "classPath"));
	}

	@Test
	void testConnRollBackAndClose() {
		DbController db = new DbController("path/to/creadentials/file.txt");
		assertFalse(db.connRollBackAndClose());

		db = new DbController(null);
		assertFalse(db.connRollBackAndClose());
	}

	@Test
	void testConnCommitAndClose() {
		DbController db = new DbController("path/to/creadentials/file.txt");
		assertFalse(db.connCommitAndClose());

		db = new DbController(null);
		assertFalse(db.connCommitAndClose());
	}

	@Test
	void testGetNewConnection() {
		DbController db = new DbController("path/to/creadentials/file.txt");
		db.getNewConnection("path/to/creadentials/file.txt");
		db.getNewConnection(null);
		db.getNewConnection("   ");
		db.getNewConnection("");
	}

}
