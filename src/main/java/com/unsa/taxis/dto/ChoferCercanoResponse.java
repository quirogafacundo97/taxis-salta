package com.unsa.taxis.dto;

import com.unsa.taxis.model.Chofer;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoferCercanoResponse {

    private Chofer chofer;

    private Double distanciaMetros;
}