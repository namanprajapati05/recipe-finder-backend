package com.example.recipefinder.service;

import com.example.recipefinder.entity.User;
import com.example.recipefinder.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String signup(User user) {

        User existingUser =
                userRepository.findByEmail(user.getEmail()).orElse(null);

        if (existingUser != null) {
            return "Email already exists";
        }

        user.setPassword(
                encoder.encode(user.getPassword())
        );

        userRepository.save(user);

        return "User Registered Successfully";
    }



    public String login(User user) {

    User existingUser =
            userRepository.findByEmail(user.getEmail()).orElse(null);

    if(existingUser == null){
        return "User not found";
    }

    boolean isMatch = encoder.matches(
            user.getPassword(),
            existingUser.getPassword()
    );

    if(!isMatch){
        return "Invalid Password";
    }

    return "Login Successful";
}
}