package org.taskana.configuration;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbScriptRunner {

	private static final Logger logger = LoggerFactory.getLogger(DbScriptRunner.class);

	private static final String SQL = "/sql";
	private static final String DB_SCHEMA = SQL + "/taskana-schema.sql";
	private static final String DB_SCHEMA_DETECTION = SQL + "/schema-detection.sql";

	private DataSource dataSource;

	private StringWriter outWriter = new StringWriter();
	private PrintWriter logWriter = new PrintWriter(outWriter);
	private StringWriter errorWriter = new StringWriter();
	private PrintWriter errorLogWriter = new PrintWriter(errorWriter);

	public DbScriptRunner(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}


	/**
	 * Run all db scripts
	 * 
	 * @throws SQLException
	 */
	public void run() throws SQLException {
		ScriptRunner runner = new ScriptRunner(dataSource.getConnection());
		logger.debug(dataSource.getConnection().getMetaData().toString());

		runner.setStopOnError(true);
		runner.setLogWriter(logWriter);
		runner.setErrorLogWriter(errorLogWriter);

		if (!isSchemaPreexisting(runner)) {
			runner.runScript(new InputStreamReader(this.getClass().getResourceAsStream(DB_SCHEMA)));
		}
		runner.closeConnection();

		logger.debug(outWriter.toString());
		if (!errorWriter.toString().trim().isEmpty()) {
			logger.error(errorWriter.toString());
		}
	}

	private boolean isSchemaPreexisting(ScriptRunner runner) {
		try {
			runner.runScript(new InputStreamReader(this.getClass().getResourceAsStream(DB_SCHEMA_DETECTION)));
		} catch (Exception e) {
			logger.debug("Schema does not exist.");
			return false;
		}
		logger.debug("Schema does exist.");
		return true;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}
