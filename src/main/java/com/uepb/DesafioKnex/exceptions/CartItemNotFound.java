package com.uepb.DesafioKnex.exceptions;

public class CartItemNotFound extends RuntimeException {
    public CartItemNotFound(Long id) {
        super("Esse item não está mais no seu carrinho. Produto id: " + id);
    }
}
