package com.restaurant.ai.security;

import com.restaurant.ai.entity.Role;
import com.restaurant.ai.entity.User;
import com.restaurant.ai.repository.RoleRepository;
import com.restaurant.ai.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AppUserDetailsService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("No account with that email."));
        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new UsernameNotFoundException("Account role is misconfigured."));

        return new AppUserPrincipal(
                user.getId(), user.getRestaurantId(), user.getEmail(), user.getPasswordHash(), role.getName(), user.isActive());
    }
}
