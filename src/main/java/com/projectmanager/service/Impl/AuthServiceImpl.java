package com.projectmanager.service.Impl;

import com.projectmanager.dto.LoginRequest;
import com.projectmanager.entity.User;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.security.jwt.JwtAuthenticationResponse;
import com.projectmanager.security.jwt.JwtUtils;
import com.projectmanager.service.AuthService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }


    public void registerUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setLastSeen(LocalDate.now());
        userRepository.save(user);
    }

    public JwtAuthenticationResponse loginUser(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetails);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User does not exist."));
        user.setOnline(true);
        user.setLastSeen(LocalDate.now());
        userRepository.save(user);
        return new JwtAuthenticationResponse(jwt,
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getName(),
                user.getRole().name(),
                user.getAvatar(),
                user.getTimezone());
    }

    @Override
    public void logout(String userId) {
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(u -> {
            u.setLastSeen(LocalDate.now());
            u.setOnline(false);
        });
        user.ifPresent(userRepository::save);
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
