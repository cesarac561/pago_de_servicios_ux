package com.bootcamp.reactive.pago_de_servicios_ux.repositories;

import com.bootcamp.reactive.pago_de_servicios_ux.entities.InputPagoFavorito;
import com.bootcamp.reactive.pago_de_servicios_ux.entities.Pago;
import reactor.core.publisher.Mono;

public interface PagoRepository {
    Mono<Pago> registrarPago(InputPagoFavorito inputPagoFavorito, String token);
}
