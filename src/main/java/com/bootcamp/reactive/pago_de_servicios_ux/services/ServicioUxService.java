package com.bootcamp.reactive.pago_de_servicios_ux.services;

import com.bootcamp.reactive.pago_de_servicios_ux.entities.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface ServicioUxService {

    Flux<Servicio> obtenerServiciosPorCanal(String canalCodigo, String token);
    Mono<Map<String,Object>> registrarPagoFavorito(InputPagoFavorito inputPagoFavorito, String token);

}
