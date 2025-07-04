package com.tezish.demo.services.userDetails;

import com.tezish.demo.model.User;
import com.tezish.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));
        if (user == null) {
            throw new UsernameNotFoundException("User Not Found with email: " + email);
        }
        return CustomUserDetails.build(user);
    }
}
