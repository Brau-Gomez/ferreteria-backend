package com.ferreteria.cart_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${PRODUCT_SERVICE_URL:http://localhost:8082}")
public interface ProductClient {
    @GetMapping("/api/products/code/{code}")
    ProductDTO getByCode(@PathVariable("code") String code);
}
