package com.orderpayment.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAutoConfiguration
@ComponentScan(basePackages= {"com.orderpayment.order.controllers", "com.orderpayment.order.dao"})
@EnableJpaRepositories(basePackages= {"com.orderpayment.order.dao"})
public class OrderClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderClientApplication.class, args);
	}
}
