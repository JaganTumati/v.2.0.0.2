package com.restaurant.ai.service;

import com.restaurant.ai.entity.Role;
import com.restaurant.ai.entity.User;
import com.restaurant.ai.exception.BusinessRuleViolationException;
import com.restaurant.ai.exception.ResourceNotFoundException;
import com.restaurant.ai.repository.RoleRepository;
import com.restaurant.ai.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(UUID restaurantId, String fullName, String email, String rawPassword, String roleName) {
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new BusinessRuleViolationException("A user with that email already exists.");
        }
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> ResourceNotFoundException.of("Role", roleName));

        User user = new User();
        user.setRestaurantId(restaurantId);
        user.setRoleId(role.getId());
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        return userRepository.save(user);
    }
}
