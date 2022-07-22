package com.example.springsecuritybug11601.web;

import com.example.springsecuritybug11601.domain.User;
import com.example.springsecuritybug11601.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes API end points for User and related operations
 *
 * @author Pavan Kumar Jadda
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Find User by username
     *
     * @return User matching the username
     *
     * @author Pavan Kumar Jadda
     * @since 1.0.0
     */
    @GetMapping("/home/{username}")
    public User getUser(@PathVariable String username) {
        return userRepository.findByUsername(username);
    }
}
