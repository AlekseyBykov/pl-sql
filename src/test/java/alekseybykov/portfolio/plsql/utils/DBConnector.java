package alekseybykov.portfolio.plsql.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Aleksey Bykov
 * @since 26.05.2020
 */
final public class DBConnector {

	private static DBConnector instance;
	private Connection connection;

	public static DBConnector getInstance() {
		if (instance == null) {
			instance = new DBConnector();
		}
		return instance;
	}

	public Connection getConnection() {
		return connection;
	}

	private DBConnector() {
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			String url = "jdbc:oracle:thin:@dbserver:1521/ora";
			String username = "scott";
			String password = "tiger";
			this.connection = DriverManager.getConnection(url, username, password);
			this.connection.setAutoCommit(false);
		} catch (ClassNotFoundException | SQLException cnfe) {
			cnfe.printStackTrace();
		}
	}
}
