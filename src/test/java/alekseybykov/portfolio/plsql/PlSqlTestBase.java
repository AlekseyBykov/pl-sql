package alekseybykov.portfolio.plsql;

import alekseybykov.portfolio.plsql.utils.DBConnector;
import alekseybykov.portfolio.plsql.utils.PerformnceAuditor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * @author Aleksey Bykov
 * @since 26.05.2020
 */
public class PlSqlTestBase {

	private static final Logger LOGGER = Logger.getLogger(PlSqlTestBase.class.getPackage().getName());

	@Rule
	public PerformnceAuditor performnceAuditor = new PerformnceAuditor();

	private static Connection connection = DBConnector.getInstance().getConnection();

	@BeforeClass
	public static void enableDbmsOutput() throws SQLException {
		System.setProperty("java.util.logging.config.file", ClassLoader.getSystemResource("logging.properties").getPath());
		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate("begin dbms_output.enable(null); end;");
		}
	}

	@AfterClass
	public static void disableDbmsOutput() throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate("begin dbms_output.disable; end;");
		}
	}

	// See for more details: https://asktom.oracle.com/pls/asktom/f?p=100:11:0::::P11_QUESTION_ID:45027262935845
	protected String perform(String plSqlCode) throws SQLException {
		try (Statement statement = connection.createStatement();
			 CallableStatement callableStatement = connection.prepareCall(
			" declare "
			+ " l_line varchar2(255); "
			+ " l_done number; "
			+ " l_buffer long; "
			+ "begin "
			+ " loop "
			+ " exit when length(l_buffer)+255 > :maxbytes OR l_done = 1; "
			+ " dbms_output.get_line( l_line, l_done ); "
			+ " l_buffer := l_buffer || l_line || chr(10); "
			+ " end loop; "
			+ " :done := l_done; "
			+ " :buffer := l_buffer; "
			+ "end;" )) {

			callableStatement.registerOutParameter( 2, java.sql.Types.INTEGER );
			callableStatement.registerOutParameter( 3, java.sql.Types.VARCHAR );

			LOGGER.info(format("Performing PL/SQL: \n%s", plSqlCode));
			statement.execute(plSqlCode);

			for(;;) {
				callableStatement.setInt( 1, 32000 );
				callableStatement.executeUpdate();
				if (callableStatement.getInt(2) == 1) {
					return callableStatement.getString(3).trim();
				}
			}
		}
	}
}
