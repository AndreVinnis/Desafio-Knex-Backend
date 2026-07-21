package com.uepb.DesafioKnex.services;

import com.uepb.DesafioKnex.dto.request.AddToCartRequest;
import com.uepb.DesafioKnex.dto.request.UpdateCartItemRequest;
import com.uepb.DesafioKnex.dto.response.CartItemResponse;
import com.uepb.DesafioKnex.dto.response.CartResponse;
import com.uepb.DesafioKnex.exceptions.CartItemNotFound;
import com.uepb.DesafioKnex.exceptions.InsufficientStockException;
import com.uepb.DesafioKnex.model.Cart;
import com.uepb.DesafioKnex.model.CartItem;
import com.uepb.DesafioKnex.model.Product;
import com.uepb.DesafioKnex.model.User;
import com.uepb.DesafioKnex.repository.CartItemRepository;
import com.uepb.DesafioKnex.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductService productService;

    @Transactional
    public CartResponse getMyCart(User user) {
        Cart cart = cartRepository.findByClientId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().client(user).build()));
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(User user, AddToCartRequest request) {
        Cart cart = getOrCreateCart(user);

        Product product = productService.getProductById(request.productId());
        assertStock(product, request.quantity());

        cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), product.getId())
                .ifPresentOrElse(
                        item -> {
                            int newQty = item.getQuantity() + request.quantity();
                            assertStock(product, newQty);
                            item.setQuantity(newQty);
                        },
                        () -> {
                            CartItem line = CartItem.builder()
                                    .cart(cart)
                                    .product(product)
                                    .quantity(request.quantity())
                                    .build();
                            cart.getItems().add(line);
                        }
                );

        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(User user, Long productId, UpdateCartItemRequest request) {
        if (request.quantity() == 0) {
            return removeItem(user, productId);
        }

        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFound(productId));

        Product product = productService.getProductById(productId);
        assertStock(product, request.quantity());
        item.setQuantity(request.quantity());

        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(User user, Long productId) {
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFound(productId));

        cart.getItems().remove(item);
        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse clearMyCart(User user) {
        return cartRepository.findByClientId(user.getId())
                .map(cart -> {
                    cart.getItems().clear();
                    cartRepository.save(cart);
                    return toResponse(cart);
                })
                .orElse(
                        toResponse(getOrCreateCart(user))
                );
    }


    private Cart getOrCreateCart(User user) {
        return cartRepository.findByClientId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().client(user).build()));
    }

    private void assertStock(Product product, int desiredQuantity) {
        if (desiredQuantity < 0) {
            throw new IllegalArgumentException("Quantidade informado menor que 0. Quantidade: " + desiredQuantity);
        }
        int available = product.getStockQuantity();
        if (available < desiredQuantity) {
            throw new InsufficientStockException(
                    "Estoque insuficiente para o produto " + product.getId() + ". Disponível: " + available
            );
        }
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemsResponse = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for(CartItem item: cart.getItems()){
            CartItemResponse itemResponse = new CartItemResponse(
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getProduct().getPrice(),
                    item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            );
            subtotal = subtotal.add(itemResponse.subtotal());
            itemsResponse.add(itemResponse);
        }
        return new CartResponse(cart.getId(), subtotal, itemsResponse);
    }

}
