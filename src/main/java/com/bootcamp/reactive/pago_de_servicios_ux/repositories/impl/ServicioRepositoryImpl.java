package com.bootcamp.reactive.pago_de_servicios_ux.repositories.impl;

import com.bootcamp.reactive.pago_de_servicios_ux.core.exceptions.ServicioUxBaseException;
import com.bootcamp.reactive.pago_de_servicios_ux.entities.Servicio;
import com.bootcamp.reactive.pago_de_servicios_ux.repositories.ServicioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Repository
public class ServicioRepositoryImpl implements ServicioRepository {

    private final WebClient client;

    private final ReactiveRedisOperations<String, Servicio> redisOperations;
    private final ReactiveHashOperations<String, String, Servicio> hashOperations;

    public ServicioRepositoryImpl(WebClient.Builder builder,
                                  @Value( "${application.urlApiServicios:http://localhost/servicios}" ) String urlApiServicios,
                                  ReactiveRedisOperations<String, Servicio> redisOperations){
        log.info("urlApiServicios = " + urlApiServicios);

        this.redisOperations = redisOperations;
        this.hashOperations = redisOperations.opsForHash();

//        this.client = builder.baseUrl(urlApiServicios)
//        .build();

        // Configurar Response timeout
        HttpClient client = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5));
        this.client = builder.baseUrl(urlApiServicios)
                .clientConnector(new ReactorClientHttpConnector(client))
                .build();
    }

    @Override
    public Flux<Servicio> obtenerServiciosPorCanal(String canalCodigo, String token) {

        Boolean existeCache = false;
        try {
            existeCache = hashOperations.values(canalCodigo).hasElements().toFuture().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if(existeCache){
            log.info("obtenerServiciosPorCanal(): Obtuvo servicios del cache");
            return hashOperations.values(canalCodigo);
        }

//        Flux<Servicio> servicioFlux = this.client.get().uri("/codigo_canal/{canalCodigo}", canalCodigo).accept(MediaType.APPLICATION_JSON)
//                .header("Authorization", token)
//                .retrieve()
//                .bodyToFlux(Servicio.class);

        Flux<Servicio> servicioFlux = this.client.get().uri("/codigo_canal/{canalCodigo}", canalCodigo).accept(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, response-> Mono.error(new ServicioUxBaseException(HttpStatus.REQUEST_TIMEOUT,"Server error")))
                .bodyToFlux(Servicio.class)
                .retryWhen(
                        Retry.fixedDelay(2, Duration.ofSeconds(2))
                                .doBeforeRetry(x->  log.info("Servicio: LOG BEFORE RETRY=" + x))
                                .doAfterRetry(x->  log.info("Servicio: LOG AFTER RETRY=" + x))
                )
                .doOnError(x-> log.info("Servicio: LOG ERROR"))
                .doOnComplete(() -> log.info("Servicio: LOG SUCCESS"));

        Map<String, Servicio> map = null;
        try {
            map = servicioFlux
                .collectMap(s -> s.getCodigo(),s -> s)
                .toFuture().get();

            hashOperations.putAll(canalCodigo,map).toFuture().get();
            redisOperations.expire(canalCodigo,Duration.ofMinutes(5)).toFuture().get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return servicioFlux;

    }
}
