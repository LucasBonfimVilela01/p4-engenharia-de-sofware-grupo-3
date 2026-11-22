package com.example.livraria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.livraria", "br.com.livraria"})
public class LivrariaApplication {
    public static void main(String[] args) {
        SpringApplication.run(LivrariaApplication.class, args);
    }
}