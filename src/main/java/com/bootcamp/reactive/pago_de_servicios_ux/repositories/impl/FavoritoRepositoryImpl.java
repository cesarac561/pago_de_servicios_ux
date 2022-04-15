package com.bootcamp.reactive.pago_de_servicios_ux.repositories.impl;

import com.bootcamp.reactive.pago_de_servicios_ux.core.exceptions.ServicioUxBaseException;
import com.bootcamp.reactive.pago_de_servicios_ux.core.exceptions.ServicioUxClientException;
import com.bootcamp.reactive.pago_de_servicios_ux.core.exceptions.ServicioUxServerException;
import com.bootcamp.reactive.pago_de_servicios_ux.entities.Favorito;
import com.bootcamp.reactive.pago_de_servicios_ux.entities.InputFavorito;
import com.bootcamp.reactive.pago_de_servicios_ux.repositories.FavoritoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import reactor.netty.http.client.HttpClient;
import java.time.Duration;

@Slf4j
@Repository
public class FavoritoRepositoryImpl implements FavoritoRepository {

    private final WebClient client;

    public FavoritoRepositoryImpl(WebClient.Builder builder,
                                  @Value( "${application.urlApiFavoritos:http://localhost/favoritos}" ) String urlApiFavoritos){
        log.info("urlApiFavoritos = " + urlApiFavoritos);

//        this.client = builder.baseUrl(urlApiFavoritos)
//        .build();

        // Configurar Response timeout
        HttpClient client = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5));
        this.client = builder.baseUrl(urlApiFavoritos)
                .clientConnector(new ReactorClientHttpConnector(client))
                .build();
    }

    @Override
    public Mono<Favorito> registrarFavorito(InputFavorito inputFavorito, String token) {

        return this.client.post().uri("/registrar_favorito")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .body(Mono.just(inputFavorito), InputFavorito.class)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response-> Mono.error(new ServicioUxClientException("Error en el request")))
                .onStatus(HttpStatus::is5xxServerError, response-> Mono.error(new ServicioUxServerException("Error en el server")))
                .bodyToMono(Favorito.class)
                .retryWhen(
                        Retry.fixedDelay(2, Duration.ofSeconds(2))
                                .filter(e -> e instanceof ServicioUxServerException)
                                .doBeforeRetry(x->  log.info("Favorito: LOG BEFORE RETRY=" + x))
                                .doAfterRetry(x->  log.info("Favorito: LOG AFTER RETRY=" + x))
                )
                .doOnError(x-> log.info("Favorito: LOG ERROR"))
                .doOnSuccess(x -> log.info("Favorito: LOG SUCCESS"));

    }
}
