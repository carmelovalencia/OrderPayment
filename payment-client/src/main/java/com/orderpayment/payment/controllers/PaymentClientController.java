package com.orderpayment.payment.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.orderpayment.payment.dao.User;
import com.orderpayment.payment.dao.UserRepository;

@RestController
public class PaymentClientController {
	@Autowired
	private UserRepository userRepo;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping(path = "/payment/{uid}/pay", method = RequestMethod.POST)
	public ResponseEntity<?> pay(@RequestParam("totalAmount") double totalAmount, @PathVariable("uid") String userId) {
		this.logger.info("===> /payment/" + userId + "/pay - POST");
		
		//get user info
		User user = this.userRepo.findOne(userId);

		if (user == null) {
			this.logger.info("===> Invalid User ID: " + userId);
			
			return ResponseEntity.status(HttpStatus.OK).body("NOT ACCEPTED. INVALID USER ID.");
		}

		double eWalletAmount = user.getEwalletamount();

		this.logger.info("===> Purhcase Received. Amount: " + totalAmount);
		this.logger.info("===> Current eWallet Amount: " + eWalletAmount);

		String status = "NOT ACCEPTED"; //default

		//check if user has enough money for the purchase
		if (totalAmount <= eWalletAmount) {
			status = "ACCEPTED"; //user has enough money
			eWalletAmount -= totalAmount;
			user.setEwalletamount(eWalletAmount);
			this.userRepo.save(user);
		}
		
		this.logger.info("===> Purhcase Status : " + status);
		this.logger.info("===> Balance EWalletAmount: " + eWalletAmount);

		return ResponseEntity.status(HttpStatus.OK).body(status);
	}

	@RequestMapping(path = "/info", method = RequestMethod.GET)
	public ResponseEntity<?> info() {
		this.logger.info("===> /info - GET");

		return ResponseEntity.status(HttpStatus.OK).body("I am a Payment Service...");
	}
	
	@RequestMapping(path = "/test", method = RequestMethod.GET)
	public ResponseEntity<?> runTest() {
		this.logger.info("===> /test - GET....\n");
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("Calling http://localhost:8081/payment/1/pay....<br>");
		
		//test /order/products - GET
		RestTemplate rt = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> data = new LinkedMultiValueMap<String, String>();
		data.add("totalAmount", Double.toString(1));

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(data,
				headers);
		
		String result = rt.postForEntity("http://localhost:8081/payment/1/pay", request, String.class)
				.getBody();
		
		if (result.startsWith("ACCEPTED")) {
			builder.append("SUCCESS!!");
		} else {
			builder.append("FAILED!!");
		}


		return ResponseEntity.status(HttpStatus.OK).body(builder.toString());
	}

}
