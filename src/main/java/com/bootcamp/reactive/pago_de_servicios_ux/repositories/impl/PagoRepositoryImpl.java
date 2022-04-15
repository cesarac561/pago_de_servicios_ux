package com.bootcamp.reactive.pago_de_servicios_ux.repositories.impl;

import com.bootcamp.reactive.pago_de_servicios_ux.core.exceptions.ServicioUxBaseException;
import com.bootcamp.reactive.pago_de_servicios_ux.core.exceptions.ServicioUxClientException;
import com.bootcamp.reactive.pago_de_servicios_ux.core.exceptions.ServicioUxServerException;
import com.bootcamp.reactive.pago_de_servicios_ux.entities.*;
import com.bootcamp.reactive.pago_de_servicios_ux.repositories.PagoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import reactor.netty.http.client.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

@Slf4j
@Repository
public class PagoRepositoryImpl implements PagoRepository {

    private final WebClient client;

    private final ReactiveRedisOperations<String, Servicio> redisOperations;
    private final ReactiveHashOperations<String, String, Servicio> hashOperations;

    public PagoRepositoryImpl(WebClient.Builder builder,
                              @Value( "${application.urlApiPagos:http://localhost/pagos}" ) String urlApiPagos,
                              ReactiveRedisOperations<String, Servicio> redisOperations){
        log.info("urlApiPagos = " + urlApiPagos);

        this.redisOperations = redisOperations;
        this.hashOperations = redisOperations.opsForHash();

//        this.client = builder.baseUrl(urlApiPagos)
//        .build();

        // Configurar Response timeout
        HttpClient client = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5));
        this.client = builder.baseUrl(urlApiPagos)
                .clientConnector(new ReactorClientHttpConnector(client))
                .build();
    }

    @Override
    public Mono<Pago> registrarPago(InputPagoFavorito inputPagoFavorito, String token) {

        Boolean existeServicioCache = false;
        try {
            existeServicioCache = hashOperations.get(inputPagoFavorito.getCodigoCanal(), inputPagoFavorito.getCodigoServicio())
                                        .hasElement().toFuture().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if(!existeServicioCache)
            return Mono.error(new ServicioUxBaseException("Servicio no encontrado en cache. Consultar los servicios antes de hacer esta transacción."));

        var inputPago = new InputPago();

        inputPago.setCodigoServicio(inputPagoFavorito.getCodigoServicio());
        inputPago.setNumeroSuministro(inputPagoFavorito.getNumeroSuministro());
        inputPago.setMontoPago(inputPagoFavorito.getMontoPago());

//        return this.client.post().uri("/registrar_pago")
//                .accept(MediaType.APPLICATION_JSON)
//                .header("Authorization", token)
//                .body(Mono.just(inputPago),InputPago.class)
//                .retrieve()
//                .bodyToMono(Pago.class);

        return this.client.post().uri("/registrar_pago")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .body(Mono.just(inputPago), InputPago.class)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response-> Mono.error(new ServicioUxClientException("Error en el request")))
                .onStatus(HttpStatus::is5xxServerError, response-> Mono.error(new ServicioUxServerException("Error en el server")))
                .bodyToMono(Pago.class)
                .retryWhen(
                        Retry.fixedDelay(2, Duration.ofSeconds(2))
                                .filter(e -> e instanceof ServicioUxServerException)
                                .doBeforeRetry(x->  log.info("Pago: LOG BEFORE RETRY=" + x))
                                .doAfterRetry(x->  log.info("Pago: LOG AFTER RETRY=" + x))
                )
                .doOnError(x-> log.info("Pago: LOG ERROR"))
                .doOnSuccess(x -> log.info("Pago: LOG SUCCESS"));

    }
}
