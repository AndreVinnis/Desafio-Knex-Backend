package com.uepb.DesafioKnex.exceptions;

public class ProductAlreadySold extends RuntimeException {
    public ProductAlreadySold(String message) {
        super(message);
    }
}
