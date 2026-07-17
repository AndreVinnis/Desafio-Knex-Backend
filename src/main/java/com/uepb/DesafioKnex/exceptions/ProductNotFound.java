package com.uepb.DesafioKnex.exceptions;

public class ProductNotFound extends RuntimeException {
    public ProductNotFound(Long id) {
        super("Nenhum produto encontrado com esse ID: " + id);
    }
}
