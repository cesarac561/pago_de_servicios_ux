package com.bootcamp.reactive.pago_de_servicios_ux.handlers;

import com.bootcamp.reactive.pago_de_servicios_ux.core.exceptions.ServicioUxBaseException;
import com.bootcamp.reactive.pago_de_servicios_ux.entities.*;
import com.bootcamp.reactive.pago_de_servicios_ux.services.ServicioUxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class ServicioUxHandler {

    @Autowired
    private ServicioUxService servicioUxService;

    public Mono<ServerResponse> obtenerServiciosPorCanal(ServerRequest serverRequest){
        String canalCodigo =serverRequest.pathVariable("canalCodigo");
        var tokenHeader = serverRequest.headers().header("Authorization");
        log.info("tokenHeader =" + tokenHeader);

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(servicioUxService.obtenerServiciosPorCanal(canalCodigo,tokenHeader.get(0)), Servicio.class);

    }

    public Mono<ServerResponse> registrarPagoFavorito(ServerRequest serverRequest){
        var pagoFavoritoInput= serverRequest.bodyToMono(InputPagoFavorito.class);
        var tokenHeader = serverRequest.headers().header("Authorization");
        log.info("tokenHeader =" + tokenHeader);

        return pagoFavoritoInput
                .flatMap(inputPagoFavorito-> this.servicioUxService.registrarPagoFavorito(inputPagoFavorito,tokenHeader.get(0)))
                .onErrorResume(ex -> Mono.error(new ServicioUxBaseException(ex.getMessage())))
                .flatMap(p ->
                    ServerResponse
                    .ok()
                    .contentType(APPLICATION_JSON)
                    .body(Mono.just(p), p.getClass())
                );

    }

}
