package com.example.livraria.model.strategy;

import java.math.BigDecimal;

public interface Descontavel {
    BigDecimal aplicarDesconto(BigDecimal valorTotal);
}
