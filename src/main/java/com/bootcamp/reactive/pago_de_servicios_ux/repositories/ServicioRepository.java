package com.bootcamp.reactive.pago_de_servicios_ux.repositories;

import com.bootcamp.reactive.pago_de_servicios_ux.entities.Servicio;
import reactor.core.publisher.Flux;

public interface ServicioRepository {
    Flux<Servicio> obtenerServiciosPorCanal(String canalCodigo, String token);
}
