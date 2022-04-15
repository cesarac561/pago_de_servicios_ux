package com.bootcamp.reactive.pago_de_servicios_ux.core.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServicioUxBaseException extends RuntimeException {

    private HttpStatus status  = HttpStatus.NOT_FOUND;
    private String message;

    public ServicioUxBaseException(String message){
        this.message = message;
    }


}
