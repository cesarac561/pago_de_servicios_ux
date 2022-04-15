package com.bootcamp.reactive.pago_de_servicios_ux.repositories;

import com.bootcamp.reactive.pago_de_servicios_ux.entities.Favorito;
import com.bootcamp.reactive.pago_de_servicios_ux.entities.InputFavorito;
import reactor.core.publisher.Mono;

public interface FavoritoRepository {
    Mono<Favorito> registrarFavorito(InputFavorito inputFavorito, String token);
}
