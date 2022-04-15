package com.bootcamp.reactive.pago_de_servicios_ux.entities;

import lombok.*;

import java.io.Serializable;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Servicio implements Serializable {
    private String id;
    private String codigo;
    private String nombre;
    private String canalId;
}
