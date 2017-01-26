package com.orderpayment.order.dao;

public class ProductPurchaseResponse {
	private ProductPurchase[] productPurchaseList;
	private String purchaseStatus;

	public String getPurchaseStatus() {
		return purchaseStatus;
	}

	public void setPurchaseStatus(String purchaseStatus) {
		this.purchaseStatus = purchaseStatus;
	}

	public ProductPurchase[] getProductPurchaseList() {
		return productPurchaseList;
	}

	public void setProductPurchaseList(ProductPurchase[] productPurchaseList) {
		this.productPurchaseList = productPurchaseList;
	}

}
