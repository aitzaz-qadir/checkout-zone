package com.checkout.checkout_zone;

// Import statements
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Annotations
@SpringBootApplication

// Main application class
public class CheckoutZoneApplication {

	static void main(String[] args) {
		SpringApplication.run(CheckoutZoneApplication.class, args);
	}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
