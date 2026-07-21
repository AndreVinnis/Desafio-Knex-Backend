package com.uepb.DesafioKnex.exceptions;

public class EmptyCart extends RuntimeException {
    public EmptyCart() {
        super("Seu carrinho está vazio");
    }
}
