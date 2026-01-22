package com.shopapp.shared.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to handle SPA (Single Page Application) routing.
 * Forwards all frontend routes to index.html so React Router can handle them.
 */
@Controller
public class SpaController {

    /**
     * Forward all SPA routes to index.html.
     * This allows React Router to handle client-side routing.
     */
    @GetMapping({
        "/",
        "/login",
        "/register", 
        "/products",
        "/products/{id}",
        "/cart",
        "/checkout",
        "/orders",
        "/orders/{id}",
        "/profile",
        "/become-vendor",
        "/vendor",
        "/vendor/**",
        "/admin",
        "/admin/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
