package com.bootcamp.reactive.pago_de_servicios_ux.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InputPagoFavorito {
    private String codigoCanal;
    private String codigoServicio;
    private String numeroSuministro;
    private BigDecimal montoPago;
    private String nombreFavorito;
    private String tipoFavorito;
}
