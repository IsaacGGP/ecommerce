package org.isaac.com.ecommers.security;

import org.isaac.com.ecommers.models.UserEntity;
import org.isaac.com.ecommers.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Se busca en la bd el usuario
        UserEntity usuario = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. Se mapea la entidad hacia el DTO
        UserDetailsImpl userDetailsImpl = new UserDetailsImpl();
        userDetailsImpl.setEmail(usuario.getEmail());
        userDetailsImpl.setPassword(usuario.getPassword());

        // 3. Se convierten los roles de BD a Authorities
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRole()));
        userDetailsImpl.setAuthorities(authorities);

        return userDetailsImpl;
    }
}
