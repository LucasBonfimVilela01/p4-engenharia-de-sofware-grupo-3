package com.example.livraria.model.domain;

import java.math.BigDecimal;

// Capa Dura: sem desconto pelo formato, 0%
public class CapaDura extends LivroFisico {
    @Override
    public BigDecimal getDesconto() { return BigDecimal.ZERO; }
}
