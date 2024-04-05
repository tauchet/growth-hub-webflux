package com.pragmaticos.transactions;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class Main {

    public static void main(String[] args) {

        Flux<String> flux = Flux
                        .just("Hola", "Adiós", "Pepito")
                        .map(String::toLowerCase);

        StepVerifier
                .create(flux)
                .expectNext("Hola")
                .expectNext("Adiós")
                .expectNext("Pepito");


    }

}
