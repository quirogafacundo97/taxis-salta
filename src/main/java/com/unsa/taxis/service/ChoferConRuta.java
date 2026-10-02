package com.unsa.taxis.service;

import com.unsa.taxis.model.Chofer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChoferConRuta {

    private Chofer chofer;
    private double distanciaMetros;
    private Double duracionSegundos;
}