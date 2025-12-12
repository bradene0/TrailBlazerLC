package com.liftoff.trail_blazers.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping({"/", "/health"})
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("TrailBlazers API is running");
    }
}
