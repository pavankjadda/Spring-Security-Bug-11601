package com.example.springsecuritybug11601.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes API end points for User and related operations
 *
 * @author Pavan Kumar Jadda
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    /**
     * Find User by username
     *
     * @return User matching the username
     *
     * @author Pavan Kumar Jadda
     * @since 1.0.0
     */
    @GetMapping("/")
    public String getUser() {
        return "Search works";
    }
}
