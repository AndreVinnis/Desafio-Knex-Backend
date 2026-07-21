package com.uepb.DesafioKnex.exceptions;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(Long id) {
        super("Esse item não está mais no seu carrinho. Produto id: " + id);
    }
}
