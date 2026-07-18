package com.uepb.DesafioKnex.controllers;

import com.uepb.DesafioKnex.dto.request.AuthRequest;
import com.uepb.DesafioKnex.dto.request.UserRequest;
import com.uepb.DesafioKnex.dto.response.AuthResponse;
import com.uepb.DesafioKnex.dto.response.UserResponse;
import com.uepb.DesafioKnex.model.User;
import com.uepb.DesafioKnex.security.JwtService;
import com.uepb.DesafioKnex.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest userRequest) {
        UserResponse user = userService.create(userRequest);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = authenticationManager.authenticate(usernamePassword);
        var token = jwtService.generateToken((User) auth.getPrincipal());
        var expiresIn = jwtService.getExpirationSeconds();

        return ResponseEntity.ok(new AuthResponse(token, expiresIn));
    }
}
