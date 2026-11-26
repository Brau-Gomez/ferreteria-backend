package com.ferreteria.cart_service.service;

import com.ferreteria.cart_service.client.ProductClient;
import com.ferreteria.cart_service.client.ProductDTO;
import com.ferreteria.cart_service.dto.CartItemRequestDTO;
import com.ferreteria.cart_service.dto.CartItemResponseDTO;
import com.ferreteria.cart_service.dto.CartResponseDTO;
import com.ferreteria.cart_service.exception.BadRequestException;
import com.ferreteria.cart_service.model.CartItem;
import com.ferreteria.cart_service.repository.CartItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository repository;

    @Mock
    private ProductClient productClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CartService cartService;

    @Test
    void addItemCreatesItemWithCurrentPrice() {
        when(productClient.getByCode("TORN-001")).thenReturn(product("TORN-001", "1200.00", 10, true));
        when(repository.findByUserIdAndProductCode("user-a", "TORN-001")).thenReturn(Optional.empty());
        when(repository.countByUserId("user-a")).thenReturn(0L);
        when(repository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItemResponseDTO response = cartService.addItem("user-a", request("TORN-001", 2));

        assertThat(response.getCode()).isEqualTo("TORN-001");
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getUnitPrice()).isEqualByComparingTo("1200.00");
        assertThat(response.getSubtotal()).isEqualByComparingTo("2400.00");
    }

    @Test
    void addItemIncrementsExistingItemInsteadOfCreatingAnotherRow() {
        CartItem existing = CartItem.builder()
                .userId("user-a")
                .productCode("TORN-001")
                .quantity(2)
                .unitPrice(new BigDecimal("1000.00"))
                .build();
        when(productClient.getByCode("TORN-001")).thenReturn(product("TORN-001", "1200.00", 10, true));
        when(repository.findByUserIdAndProductCode("user-a", "TORN-001")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        CartItemResponseDTO response = cartService.addItem("user-a", request("TORN-001", 2));

        assertThat(response.getQuantity()).isEqualTo(4);
        assertThat(response.getUnitPrice()).isEqualByComparingTo("1200.00");
        verify(repository).save(existing);
    }

    @Test
    void addItemRejectsQuantityAboveStockWithoutSaving() {
        when(productClient.getByCode("TORN-001")).thenReturn(product("TORN-001", "1200.00", 3, true));

        assertThatThrownBy(() -> cartService.addItem("user-a", request("TORN-001", 4)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Stock insuficiente");

        verify(repository, never()).save(any());
    }

    @Test
    void usersHaveIndependentCartRows() {
        when(productClient.getByCode("TORN-001")).thenReturn(product("TORN-001", "1200.00", 10, true));
        when(repository.findByUserIdAndProductCode(any(), eq("TORN-001"))).thenReturn(Optional.empty());
        when(repository.countByUserId(any())).thenReturn(0L);
        when(repository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cartService.addItem("user-a", request("TORN-001", 1));
        cartService.addItem("user-b", request("TORN-001", 1));

        verify(repository).findByUserIdAndProductCode("user-a", "TORN-001");
        verify(repository).findByUserIdAndProductCode("user-b", "TORN-001");
        verify(repository, times(2)).save(any(CartItem.class));
    }

    @Test
    void getCartUsesPersistedItemsWithoutCallingProductServicePerItem() {
        CartItem item = CartItem.builder()
                .userId("user-a")
                .productCode("TORN-001")
                .quantity(2)
                .unitPrice(new BigDecimal("1200.00"))
                .build();
        when(repository.findByUserIdOrderByCreatedAtAsc("user-a")).thenReturn(List.of(item));

        CartResponseDTO response = cartService.getCart("user-a");

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotal()).isEqualByComparingTo("2400.00");
        verifyNoInteractions(productClient);
    }

    private static CartItemRequestDTO request(String code, int quantity) {
        CartItemRequestDTO request = new CartItemRequestDTO();
        request.setCode(code);
        request.setQuantity(quantity);
        return request;
    }

    private static ProductDTO product(String code, String price, int stock, boolean active) {
        ProductDTO product = new ProductDTO();
        product.setCode(code);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        product.setActive(active);
        return product;
    }
}
