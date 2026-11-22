package com.example.livraria.model.strategy;

import java.math.BigDecimal;

public interface Fretavel {
    BigDecimal calcularFrete(String cep);
    int getTempo();
}
