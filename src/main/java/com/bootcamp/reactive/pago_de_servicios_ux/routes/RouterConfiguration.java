package com.bootcamp.reactive.pago_de_servicios_ux.routes;

import com.bootcamp.reactive.pago_de_servicios_ux.handlers.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouterConfiguration {

    @Bean
    public RouterFunction<ServerResponse> servicioUxRoutes(ServicioUxHandler servicioUxHandler) {
        return RouterFunctions.nest(RequestPredicates.path("pagfav"),
                RouterFunctions
                        .route(GET("/obtener_servicios/canal/{canalCodigo}"), servicioUxHandler::obtenerServiciosPorCanal)
                        .andRoute(POST("/registrar_pago_favorito").and(contentType(APPLICATION_JSON)), servicioUxHandler::registrarPagoFavorito)
            );
    }

}
