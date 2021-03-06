package net.stormdev.mario.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import net.stormdev.mario.mariokart.main;
import code.husky.mysql.MySQL;

public class SQLManager {
	MySQL MySQL = null;
	Connection c = null;

	public SQLManager(String hostName, String port, String dbName,
			String username, String password) {
		main.logger.info("Connecting to mySQL database...");
		try {
			MySQL = new MySQL(main.plugin, hostName, port, dbName, username,
					password);
			c = MySQL.openConnection();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			main.logger.info("Error connecting to mySQL database!");
		}
	}

	public synchronized void closeConnection() {
		try {
			c.close();
		} catch (SQLException e) {
			main.logger.info("Error occured when closing sql connection!");
		}
		if(MySQL != null){
			MySQL.closeConnection();
		}
		return;
	}

	public Object searchTable(String tableName, String keyName,
			String keyValue, String valueName) throws SQLException {
		Statement statement = c.createStatement();
		ResultSet res = statement.executeQuery("SELECT * FROM " + tableName
				+ " WHERE " + keyName + " = '" + keyValue + "';");
		res.next();
		Object found = null;
		if (res.getString(keyName) == null) {
			found = null;
		} else {
			found = res.getObject(valueName);
		}
		res.close();
		statement.close();
		return found;
	}

	public Map<String, Object> getFromTable(String tableName, String col1,
			String col2) throws SQLException {
		Statement statement = c.createStatement();
		ResultSet res = statement.executeQuery("SELECT " + col1 + "," + col2
				+ " FROM " + tableName + ";");
		HashMap<String, Object> results = new HashMap<String, Object>();
		while (res.next()) {
			Object found = res.getObject(col2);
			String key = res.getString(col1);
			if (key != null && found != null) {
				results.put(key, found);
			}
		}
		res.close();
		statement.close();
		return results;
	}

	public Map<String, String> getStringsFromTable(String tableName,
			String col1, String col2) throws SQLException {
		Statement statement = c.createStatement();
		ResultSet res = statement.executeQuery("SELECT " + col1 + "," + col2
				+ " FROM " + tableName + ";");
		HashMap<String, String> results = new HashMap<String, String>();
		while (res.next()) {
			String found = res.getString(col2);
			String key = res.getString(col1);
			if (key != null && found != null) {
				results.put(key, found);
			}
		}
		res.close();
		statement.close();
		return results;
	}

	public Boolean deleteFromTable(String tableName, String keyName,
			String keyValue, String valueName) throws SQLException {
		String del = "DELETE FROM " + tableName + " WHERE " + tableName + "."
				+ keyName + " = ?;";
		PreparedStatement delStatement = c.prepareStatement(del);
		delStatement.setString(1, keyValue);
		delStatement.executeUpdate();
		delStatement.close();
		return true;
	}

	public Boolean setInTable(String tableName, String keyName,
			String keyValue, String valueName, Object value)
			throws SQLException {
		// Make so it overrides key
		/*
		 * IF EXISTS( SELECT ORDER_ID FROM DBO.ORDER_DETAILS WHERE ORDER_ID =
		 * 11032 ) BEGIN DELETE FROM DBO.ORDER_DETAILS WHERE ORDER_ID = 11032
		 * END
		 */
		String del = "DELETE FROM " + tableName + " WHERE " + tableName + "."
				+ keyName + " = ?;";
		PreparedStatement delStatement = c.prepareStatement(del);
		delStatement.setString(1, keyValue);
		delStatement.executeUpdate();
		delStatement.close();
		String replace = "REPLACE INTO " + tableName + " (`" + keyName + "`, `"
				+ valueName + "`) VALUES (?, ?);";
		PreparedStatement placeStatement = c.prepareStatement(replace);
		placeStatement.setString(1, keyValue);
		placeStatement.setString(2, value + "");
		placeStatement.executeUpdate();
		placeStatement.close();
		return true;
	}

	public void createTable(String tableName, String[] columns, String[] types) {
		String query = null;
		try {
			query = "CREATE TABLE IF NOT EXISTS " + tableName + "(";
			Boolean first = true;
			for (int i = 0; i < columns.length && i < types.length; i++) {
				if (!first) {
					query = query + ", ";
				} else {
					first = false;
				}
				String col = columns[i];
				String type = types[i];
				query = query + col + " " + type;
			}
			query = query + ");";
			// Query is assembles
			Statement statement = c.createStatement();
			statement.executeUpdate(query);
			statement.close();
		} catch (SQLException e) {
			main.logger.info(main.colors.getError() + "Query: " + query);
			main.logger.info(main.colors.getError() + "Error: "
					+ e.getMessage());
		}
	}
}
