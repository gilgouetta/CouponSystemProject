package com.sys.dao;

import com.sys.beans.Customer;
import com.sys.exception.CouponException;
import com.sys.exception.CouponSystemException;

public interface ElementDAO<T> extends DAO<T> {
	boolean exists(int customerId, int couponId) throws CouponSystemException;
	void addPurchase(int couponId,Customer customer) throws CouponSystemException;
	void deletePurchase(int couponId,Customer customer) throws CouponSystemException;
	void deleteCouponsOfCustomer(int customerId) throws CouponException;
	void deleteAllFromHistory(int couponId) throws CouponException;
}
