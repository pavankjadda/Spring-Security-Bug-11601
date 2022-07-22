package com.example.springsecuritybug11601.security.userdetails;


import com.example.springsecuritybug11601.domain.User;
import com.example.springsecuritybug11601.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class PresDbUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public PresDbUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Gets UserDetails object based on provided username
     *
     * @param username username of the User in PRES database
     *
     * @return {@link UserDetails} object
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new UsernameNotFoundException("Could not find Username in the database");
        return new PresDbUserDetails(user, user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).toList());
    }
}
