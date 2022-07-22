package com.example.springsecuritybug11601.repository;

import com.example.springsecuritybug11601.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByUsername(String username);
}
