package com.bootcamp.reactive.pago_de_servicios_ux.entities;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Favorito {
    private Integer id;
    private String nombre;
    private String tipoFavorito;
    private String suministroId;

}
