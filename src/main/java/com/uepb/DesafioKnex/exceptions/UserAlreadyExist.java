package com.uepb.DesafioKnex.exceptions;

public class UserAlreadyExist extends RuntimeException {
    public UserAlreadyExist(String email) {
        super("Já existe um usuário com esse email: " + email);
    }
}
