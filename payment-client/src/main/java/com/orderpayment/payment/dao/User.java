package com.orderpayment.payment.dao;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User {
	@Id
	private String id;

	private double ewalletamount;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getEwalletamount() {
		return ewalletamount;
	}

	public void setEwalletamount(double ewalletamount) {
		this.ewalletamount = ewalletamount;
	}

}
