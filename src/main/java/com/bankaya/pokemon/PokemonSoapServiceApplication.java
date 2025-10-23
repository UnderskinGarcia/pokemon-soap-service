package com.bankaya.pokemon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class PokemonSoapServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokemonSoapServiceApplication.class, args);
    }

}
