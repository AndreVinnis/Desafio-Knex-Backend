package com.uepb.DesafioKnex.services;

import com.uepb.DesafioKnex.dto.request.UserRequest;
import com.uepb.DesafioKnex.dto.response.UserResponse;
import com.uepb.DesafioKnex.exceptions.UserAlreadyExist;
import com.uepb.DesafioKnex.model.User;
import com.uepb.DesafioKnex.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Transactional
    public UserResponse create(UserRequest userRequest){
        if(userRepository.findByEmail(userRequest.email()) != null){
            throw new UserAlreadyExist(userRequest.email());
        }
        if(!isValidEmail(userRequest.email())){
            throw new IllegalArgumentException("Email inválido");
        }
        User newUser = User.builder()
                .name(userRequest.name())
                .email(userRequest.email())
                .hashPassword(encoder.encode(userRequest.password()))
                .role(userRequest.role())
                .build();

        return toResponse(userRepository.save(newUser));
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private UserResponse toResponse(User user){
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt());
    }
}
