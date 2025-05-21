package com.gaby.projetblogrecettes.service;

import com.gaby.projetblogrecettes.model.User;
import com.gaby.projetblogrecettes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // ici déjà encodé en base
                .roles(user.getRole().name().replace("ROLE_", ""))
                .build();
    }

    // (Optionnel) méthode utilitaire si tu veux encoder un mot de passe ici
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
