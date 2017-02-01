package com.orderpayment.order.controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderpayment.order.dao.Product;
import com.orderpayment.order.dao.ProductPurchase;
import com.orderpayment.order.dao.ProductPurchaseResponse;
import com.orderpayment.order.dao.ProductRepository;

@RestController
public class OrderClientController {
	@Autowired
	private ProductRepository productRepo;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping(path = "/order/products", method = RequestMethod.GET)
	public ResponseEntity<?> getProducts() {
		this.logger.info("===> /order/products - GET");

		// get all products
		Iterator<Product> it = this.productRepo.findAll().iterator();

		List<Product> list = new ArrayList<Product>();

		while (it.hasNext()) {
			list.add(it.next());
		}

		return ResponseEntity.status(HttpStatus.OK).body(list);
	}

	@RequestMapping(path = "order/products/{uid}/purchase", method = RequestMethod.POST)
	public ResponseEntity<?> purchaseProducts(@RequestParam("productPurchase") String productPurchaseJson,
			@PathVariable("uid") String userId) {
		this.logger.info("===> /order/products/" + userId + "/purchase - POST");
		this.logger.info("===> Received Product Purchase: " + productPurchaseJson);

		// convert json string to object
		ObjectMapper mapper = new ObjectMapper();

		ProductPurchase[] productPurchase = null;

		try {
			productPurchase = mapper.readValue(productPurchaseJson, ProductPurchase[].class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.logger.error(e.getLocalizedMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}

		// add the total amount purchase
		double totalAmount = 0.0;

		for (ProductPurchase pp : productPurchase) {
			if (pp.getQuantity() > 0) {
				totalAmount += pp.getPrice() * pp.getQuantity();
			}
		}

		this.logger.info("===> Making Payment for Amount: " + totalAmount);

		// Create a rest template and call the payment service
		RestTemplate rt = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> data = new LinkedMultiValueMap<String, String>();
		data.add("totalAmount", Double.toString(totalAmount));

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(data,
				headers);

		try {
			String status = rt.postForEntity("http://localhost:8081/payment/" + userId + "/pay", request, String.class)
					.getBody();

			// Compose the reponse to user
			ProductPurchaseResponse response = new ProductPurchaseResponse();
			response.setProductPurchaseList(productPurchase);
			response.setPurchaseStatus(status);

			this.logger.info("===> Payment Status: " + status);

			return ResponseEntity.status(HttpStatus.OK).body(response);
		} catch (Exception ex) {
			this.logger.info("===> Payment Failed. Reason : " + ex.getLocalizedMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getLocalizedMessage());
		}
	}

	@RequestMapping(path = "/info", method = RequestMethod.GET)
	public ResponseEntity<?> info() {
		this.logger.info("===> /info - GET");

		return ResponseEntity.status(HttpStatus.OK).body("I am an Order Service...");
	}

	@RequestMapping(path = "/test", method = RequestMethod.GET)
	public ResponseEntity<?> runTest() {
		this.logger.info("===> /test - GET....\n");

		StringBuilder builder = new StringBuilder();

		String listProduct = this.getAllProducts();
		builder.append(listProduct);
		builder.append("<br>");

		String purchaseProduct = this.purchaseProduct();
		builder.append(purchaseProduct);

		return ResponseEntity.status(HttpStatus.OK).body(builder.toString());
	}

	// retrieves all products
	private String getAllProducts() {
		StringBuilder builder = new StringBuilder();

		builder.append("Calling http://localhost:8080/order/products....<br>");

		// test /order/products - GET
		RestTemplate rt = new RestTemplate();

		String result = rt.getForEntity("http://localhost:8080/order/products", String.class).getBody();

		ObjectMapper mapper = new ObjectMapper();

		Product[] products = null;

		try {
			products = mapper.readValue(result, Product[].class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.logger.error(e.getLocalizedMessage());
			builder.append("FAILED!!<br>");
			builder.append(e.getLocalizedMessage());
			return builder.toString();
		}

		// there should be 5 products returned
		if (products != null && products.length == 5) {
			builder.append("SUCCESS!!");
		} else {
			builder.append("FAILED!!");
		}

		return builder.toString();
	}

	// purhcases a product
	private String purchaseProduct() {
		StringBuilder builder = new StringBuilder();

		builder.append("Calling http://localhost:8080/order/products/1/purchase....<br>");

		// test /order/products - GET
		RestTemplate rt = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> data = new LinkedMultiValueMap<String, String>();
		data.add("productPurchase",
				"[{\"id\": \"1\",\"title\": \"Black Watch Strap\",\"description\": \"Leather Black Watch Strap\",\"price\": 1.25,\"quantity\":2}]");

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(data,
				headers);

		String status = rt.postForEntity("http://localhost:8080/order/products/1/purchase", request, String.class)
				.getBody();

		ObjectMapper mapper = new ObjectMapper();
		ProductPurchaseResponse response = null;

		try {
			response = mapper.readValue(status, ProductPurchaseResponse.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.logger.error(e.getLocalizedMessage());
			builder.append("FAILED!!<br>");
			builder.append(e.getLocalizedMessage());
			return builder.toString();
		}

		if (response.getPurchaseStatus().startsWith("ACCEPTED")) {
			builder.append("SUCCESS!!");
		} else {
			builder.append("FAILED!!");
		}

		return builder.toString();
	}

}
