package com.projectmanager.service.Impl;

import com.projectmanager.entity.User;
import com.projectmanager.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = null;

        // Try to find by username first (for token validation)
        if (userRepository.existsByUsername(identifier)) {
            user = userRepository.findByUsername(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + identifier));
        }
        // Fall back to email (for login authentication)
        else if (userRepository.existsByEmail(identifier)) {
            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + identifier));
        } else {
            throw new UsernameNotFoundException("User not found with identifier: " + identifier);
        }

        return UserDetailsImpl.build(user);
    }
}