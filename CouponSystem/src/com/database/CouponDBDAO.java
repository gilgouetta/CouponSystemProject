package com.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.database.exception.CouponException;
import com.database.exception.CouponSystemException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CouponDBDAO implements ElementDAO<Coupon> {
//TODO Change the sqlCreate Statement
	private static String sqlCreate = "insert into coupons "
			+ "(company_id,category_id,title,start_date,end_date,amount,type,description,price,image) " + "values (?,?,?,?,?,?,?,?,?,?)";

	private static String sqlRead = "select * from coupons where id = ?";

	private static String sqlUpdate = "update coupons set " + "title = ?, Start_date = ?, end_date = ?,"
			+ "amount = ? , category = ? , description = ?,"  + "company_id = ? , category_id = ? ,"+ "price = ? ,image = ? where id = ?";

	private static String sqlDelete = "delete from coupons where id = ?";

	private Connection connection;

	@Override
	public void create(Coupon coupon) throws CouponSystemException {
		
//		 "insert into coupons "
//					+ "(company_id,category_id,title,start_date,end_date,amount,type,description,price,image) " + "values (?,?,?,?,?,?,?,?,?,?)"
		
		connect();
		try (PreparedStatement create = connection.prepareStatement(sqlCreate)) {
			java.sql.Date startDate = (Date) coupon.getStartDate();
			java.sql.Date endDate = (Date) coupon.getEndDate();
			create.setInt(1, coupon.getCompanyId());
			create.setInt(2, coupon.getCategoryId());
			create.setString(3, coupon.getTitle());
			create.setDate(4, startDate);
			create.setDate(5, endDate);
			create.setInt(6, coupon.getAmount());
			create.setString(7, coupon.getCouponType().toString());
			create.setString(8, coupon.getMessage());
			create.setDouble(9, coupon.getPrice());
			create.setString(10, coupon.getImage());

			create.execute();
		} catch (SQLException e) {
			throw new CouponSystemException("error in creating coupon",e);
		}
		finally {disconnect();}
	}

	@Override
	public Coupon read(int id) throws CouponSystemException {
		// ("select * from ? where id = ?")
		Coupon result = null;
		connect();
		try (PreparedStatement read = connection.prepareStatement(sqlRead)) {
			read.setString(1, "coupon");
			read.setInt(2, id);
			ResultSet rs = read.executeQuery();
			if (rs.next()) {
				result = readFromActiveConnection(id, rs);
			}

		} catch (SQLException e) {
			throw new CouponSystemException("error in reading coupon",e);
		}
		finally {disconnect();}
		return result;
	}

	private Coupon readFromActiveConnection(int id, ResultSet rs) throws SQLException {
		Coupon result;
		result = new Coupon();

		int amount = rs.getInt("amount");
		String title = rs.getString("title");
		String message = rs.getString("message");
		Category type = Category.valueOf(rs.getString("type"));
		double price = rs.getDouble("price");
		Date startDate = rs.getDate("start_date");
		Date endDate = rs.getDate("end_date");
		String image = rs.getString("image");
		result.setAmount(amount);
		result.setCouponType(type);
		result.setEndDate(endDate);
		result.setStartDate(startDate);
		result.setId(id);
		result.setImage(image);
		result.setMessage(message);
		result.setPrice(price);
		result.setTitle(title);
		return result;
	}

	@Override
	public void update(Coupon coupon) throws CouponSystemException {

//		"update coupons set " + "title = ?, Start_date = ?, end_date = ?,"
//				+ "amount = ? , category = ? , description = ?,"  + "company_id = ? , category_id = ? ,"+ "price = ? ,image = ? where id = ?"

		connect();
		try (PreparedStatement update = connection.prepareStatement(sqlUpdate)) {
			update.setString(1, coupon.getTitle());
			update.setDate(2, (Date) coupon.getStartDate());
			update.setDate(3, (Date) coupon.getEndDate());
			update.setInt(4, coupon.getAmount());
			update.setString(5, coupon.getCategory().toString());
			update.setString(6, coupon.getDescription());
			update.setInt(7, coupon.getCompanyId());
			update.setInt(8, coupon.getCategoryId());
			update.setDouble(9, coupon.getPrice());
			update.setString(10, coupon.getImage());
			update.setInt(11, coupon.getId());
			update.execute();
		} catch (SQLException e) {
			throw new CouponSystemException("error in updating coupon " + coupon ,e);
		} finally {
			disconnect();
		}
	}

	@Override
	public void delete(int id) throws CouponSystemException {
		connect();
		try (PreparedStatement delete = connection.prepareStatement(sqlDelete)) {
			delete.setInt(1, id);
			delete.execute();
		} catch (SQLException e) {
			throw new CouponSystemException("error in deleting coupon",e);
		} finally {
			disconnect();
		}
	}

	@Override
	public Collection<Coupon> readAll() throws CouponSystemException {
		List<Coupon> result = new ArrayList<>();
		connect();
		try (Statement readAll = connection.createStatement()) {
			String sql = "select * from coupons";
			ResultSet rs = readAll.executeQuery(sql);
			while (rs.next()) {
				result.add(readFromActiveConnection(rs.getInt("id"), rs));
			}

		} catch (SQLException e) {
			throw new CouponSystemException("error in reading all coupons",e);
		} finally {
			disconnect();
		}
		return result;
	}

	public Collection<Coupon> readAll(Company company) throws CouponSystemException {
		List<Coupon> result = new ArrayList<>();
		connect();
		try (Statement readAll = connection.createStatement()) {
			String sql = "select * from coupons where company_id = " + company.getId();
			ResultSet rs = readAll.executeQuery(sql);
			while (rs.next()) {
				result.add(readFromActiveConnection(rs.getInt("id"), rs));
			}

		} catch (SQLException e) {
			throw new CouponSystemException("Exception raised in reading all coupons",e);
		} finally {
			disconnect();
		}

		return result;
	}


	private synchronized void connect() throws CouponSystemException {
		connection = ConnectionPool.getInstance().getConnection();
	}

	private synchronized void disconnect() throws CouponSystemException {
		ConnectionPool.getInstance().restoreConnection(connection);
		connection = null;
	}

	@Override
	public boolean exists(int customerId, int couponId) throws CouponSystemException {
		boolean result = false;
		connect();
		String preparedSql = "select * from customers_vs_coupons where customer_id = ? , coupon_id = ?";
		try(PreparedStatement read = connection.prepareStatement(preparedSql)){
			read.setInt(1, customerId);
			read.setInt(2, couponId);
			ResultSet rs = read.executeQuery();
			result = rs.next();
			
		} catch (SQLException e) {
			throw new CouponException("error in fetching coupon from customers_vs_coupons",e);
		}
		finally {
			disconnect();
		}
		return result;
	}

	@Override
	public void addPurchase(int couponId) throws CouponSystemException {
		readAndIncrement(couponId,1);
	}



	@Override
	public void deletePurchase(int couponId) throws CouponSystemException {
		readAndIncrement(couponId, -1);
		}
	
	private void readAndIncrement(int couponId,int increment) throws CouponSystemException {
		Coupon coupon = read(couponId);
		coupon.setAmount(coupon.getAmount()+increment);
		update(coupon);
	}
}