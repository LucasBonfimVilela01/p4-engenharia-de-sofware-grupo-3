package com.example.livraria.model.domain;

import java.math.BigDecimal;

// Brochura: 5% de desconto
public class Brochura extends LivroFisico {
    @Override
    public BigDecimal getDesconto() {
        return new BigDecimal("0.05"); }
}


