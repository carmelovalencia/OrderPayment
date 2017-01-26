package com.orderpayment.payment.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.orderpayment.payment.dao.User;
import com.orderpayment.payment.dao.UserRepository;

@RestController
public class PaymentClientController {
	@Autowired
	private UserRepository userRepo;

	@RequestMapping(path = "/payment/{uid}/pay", method = RequestMethod.POST)
	public ResponseEntity<?> pay(@RequestParam("totalAmount") double totalAmount, @PathVariable("uid") String userId) {
		User user = this.userRepo.findOne(userId);
		
		if (user == null) {
			return ResponseEntity.status(HttpStatus.OK).body("NOT ACCEPTED. INVALID USER ID.");
		}
		
		double eWalletAmount = user.getEwalletamount();
		
		System.out.println("Purhcase Received. Amount: " + totalAmount);
		System.out.println("Current eWallet Amount: " + eWalletAmount);

		String status = "NOT ACCEPTED";

		if (totalAmount <= eWalletAmount) {
			status = "ACCEPTED";
			eWalletAmount -= totalAmount;
			user.setEwalletamount(eWalletAmount);
			this.userRepo.save(user);
		}

		System.out.println("Purhcase Status : " + status);
		System.out.println("Balance EWalletAmount: " + eWalletAmount);

		return ResponseEntity.status(HttpStatus.OK).body(status);
	}

	@RequestMapping(path = "/info", method = RequestMethod.GET)
	public ResponseEntity<?> info() {
		return ResponseEntity.status(HttpStatus.OK).body("I am a Payment Service...");
	}
}
