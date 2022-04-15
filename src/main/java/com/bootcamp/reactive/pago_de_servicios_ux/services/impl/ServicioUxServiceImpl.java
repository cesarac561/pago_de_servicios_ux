package com.bootcamp.reactive.pago_de_servicios_ux.services.impl;

import com.bootcamp.reactive.pago_de_servicios_ux.entities.*;
import com.bootcamp.reactive.pago_de_servicios_ux.repositories.FavoritoRepository;
import com.bootcamp.reactive.pago_de_servicios_ux.repositories.PagoRepository;
import com.bootcamp.reactive.pago_de_servicios_ux.repositories.ServicioRepository;
import com.bootcamp.reactive.pago_de_servicios_ux.services.ServicioUxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class ServicioUxServiceImpl implements ServicioUxService {

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Override
    public Flux<Servicio> obtenerServiciosPorCanal(String canalCodigo, String token) {
        return this.servicioRepository.obtenerServiciosPorCanal(canalCodigo, token);
    }

    @Override
    public Mono<Map<String,Object>> registrarPagoFavorito(InputPagoFavorito inputPagoFavorito, String token) {

        Map<String,Object> respuesta = new HashMap<String,Object>();
        var inputFavorito = new InputFavorito();

        var monoPago = this.pagoRepository.registrarPago(inputPagoFavorito, token);

        Mono<Favorito> monoFavorito = null;

        if (inputPagoFavorito.getNombreFavorito() != null && !inputPagoFavorito.getNombreFavorito().isEmpty() && !inputPagoFavorito.getNombreFavorito().isBlank()) {
            inputFavorito.setNombre(inputPagoFavorito.getNombreFavorito());
            inputFavorito.setTipoFavorito(inputPagoFavorito.getTipoFavorito());
            inputFavorito.setCodigoServicio(inputPagoFavorito.getCodigoServicio());
            inputFavorito.setNumeroSuministro(inputPagoFavorito.getNumeroSuministro());
            monoFavorito = this.favoritoRepository.registrarFavorito(inputFavorito, token);
        }

        Mono<Favorito> finalMonoFavorito = monoFavorito;

        if (finalMonoFavorito != null) {
            return monoPago
                    .flatMap(p -> finalMonoFavorito.map(f -> Pair.of(p, f)))
                    .flatMap(PagoFavoritoPair -> {
                        var pago = PagoFavoritoPair.getFirst();
                        var favorito = PagoFavoritoPair.getSecond();
                        respuesta.put("pago", pago);
                        respuesta.put("favorito", favorito);
                        return Mono.just(respuesta);
                    });
        }

        return monoPago
                .flatMap(p -> {
                    respuesta.put("pago", p);
                    return Mono.just(respuesta);

                });

    }

}
