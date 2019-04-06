
package com.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomerDBDAO implements DAO<Customer> {

	private Connection connection;

	private static String sqlCreate = "insert into customers (first_name,last_name,password,email) VALUES(?,?,?,?)";
	private static String sqlRead = "select * from customers where id = ?";
	private static String sqlUpdate = "update customers set first_name = ? , last_name = ? , password = ? , email = ? WHERE id = ?";
	private static String sqlDelete = "delete from customers where id = ?";

	@Override
	public boolean exists(String...arguments) throws CouponSystemException {
		boolean result = false;
		connect();
		String email = arguments[0];
		String password = arguments[1];
		if(email.isEmpty() || password.isEmpty()) {
			throw new CouponSystemException("Password or Email were empty");
		}
		try (Statement stmt = connection.createStatement()) {
			String sql = "select * from customers where email = '" + email + "'" + " and password = '" + password + "'";
			ResultSet rs = stmt.executeQuery(sql);
			result = rs.next();
			disconnect();
		} catch (SQLException e) {
			DbExceptionHandler.HandleException(e);
		}

		return result;
	}

	@Override
	public void create(Customer customer) {
		// "insert into customers (first_name,last_name,password,email) VALUES(?,?,?,?)"
		connect();
		try (PreparedStatement create = connection.prepareStatement(sqlCreate)) {
			create.setString(1, customer.getFirstName());
			create.setString(2, customer.getLastName());
			create.setString(3, customer.getPassword());
			create.setString(4, customer.getEmail());
			create.execute();

			disconnect();
		} catch (SQLException e) {
			DbExceptionHandler.HandleException(e);
		}
	}

	@Override
	public Customer read(int id) {
		// "select * from customers where id = ?"
		Customer result = null;
		connect();
		try {
			result = readFromConnection(id);
			disconnect();
		} catch (SQLException e) {
			DbExceptionHandler.HandleException(e);
		}
		return result;
	}




	@Override
	public void update(Customer customer) {
		// "update customers set first_name = ?
		// , last_name = ? , password = ? , email = ? WHERE id = ?"
		connect();
		try (PreparedStatement update = connection.prepareStatement(sqlUpdate)) {
//			Customer customer = readFromConnection(id);
			update.setString(1, customer.getFirstName());
			update.setString(2, customer.getLastName());
			update.setString(3, customer.getPassword());
			update.setString(4, customer.getEmail());
			update.setInt(5, customer.getId());
			update.execute();
			disconnect();
		} catch (SQLException e) {
			DbExceptionHandler.HandleException(e);
		}

	}

	@Override
	public void delete(int id) {
		connect();
		try(PreparedStatement delete = connection.prepareStatement(sqlDelete)){
			delete.setInt(1, id);
			delete.execute();
			disconnect();
		} catch (SQLException e) {
			DbExceptionHandler.HandleException(e);
		}
	}

	@Override
	public Collection<Customer> readAll() {
		List<Customer> result = new ArrayList<>();
		connect();
		String sqlReadAll = "select * from customers";
		try(Statement readAll = connection.createStatement()){
			ResultSet rs = readAll.executeQuery(sqlReadAll);
			while(rs.next()) {
				result.add(createCustomer(rs.getInt("id"), rs));
			}
			
			disconnect();
		} catch (SQLException e) {
			DbExceptionHandler.HandleException(e);
		}
		return result;
	}

	private synchronized void connect() {
		if (connection == null) {
			connection = ConnectionPool.getInstance().getConnection();
		}
	}

	private synchronized void disconnect() throws SQLException {
		ConnectionPool.getInstance().restoreConnection(connection);
		connection = null;
	}
	
	private Customer readFromConnection(int id) {
		Customer result = null;
		try (PreparedStatement read = connection.prepareStatement(sqlRead)) {
			read.setInt(1, id);
			ResultSet rs = read.executeQuery();
			if (rs.next()) {
				result = createCustomer(id, rs);
			}

		} catch (SQLException e) {
			DbExceptionHandler.HandleException(e);
		}
		return result;
	}

	
	private Customer createCustomer(int id, ResultSet rs) throws SQLException {
		Customer result;
		result = new Customer(id);
		result.setFirstName(rs.getString("first_name"));
		result.setLastName(rs.getString("last_name"));
		result.setEmail(rs.getString("email"));
		result.setPassword(rs.getString("password"));
		return result;
	}
}