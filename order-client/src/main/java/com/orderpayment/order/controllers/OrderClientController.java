package com.orderpayment.order.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderpayment.order.dao.Product;
import com.orderpayment.order.dao.ProductPurchase;
import com.orderpayment.order.dao.ProductPurchaseResponse;
import com.orderpayment.order.dao.ProductRepository;

@RestController
public class OrderClientController {
	@Autowired
	private ProductRepository productRepo;

	@RequestMapping(path = "/order/products", method = RequestMethod.GET)
	public ResponseEntity<?> getProducts() {
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
		System.out.println("Received Product Purchase: " + productPurchaseJson);

		ObjectMapper mapper = new ObjectMapper();

		ProductPurchase[] productPurchase = null;

		try {
			productPurchase = mapper.readValue(productPurchaseJson, ProductPurchase[].class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}

		double totalAmount = 0.0;

		for (ProductPurchase pp : productPurchase) {
			if (pp.getQuantity() > 0) {
				totalAmount += pp.getPrice() * pp.getQuantity();
			}
		}

		System.out.println("Making Payment for Amount: " + totalAmount);

		RestTemplate rt = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> data = new LinkedMultiValueMap<String, String>();
		data.add("totalAmount", Double.toString(totalAmount));

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(data,
				headers);

		String status = rt.postForEntity("http://localhost:8081/payment/" + userId + "/pay", request, String.class)
				.getBody();

		ProductPurchaseResponse response = new ProductPurchaseResponse();
		response.setProductPurchaseList(productPurchase);
		response.setPurchaseStatus(status);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@RequestMapping(path = "/info", method = RequestMethod.GET)
	public ResponseEntity<?> info() {
		return ResponseEntity.status(HttpStatus.OK).body("I am an Order Service...");
	}
}
