package com.example.livraria.model.strategy;

import java.math.BigDecimal;

// Pix: 8% de desconto à vista
public class PIX implements Descontavel {
    @Override
    public BigDecimal aplicarDesconto(BigDecimal valorTotal) {
        BigDecimal desconto = new BigDecimal("0.08");
        return valorTotal.multiply(BigDecimal.ONE.subtract(desconto));
    }
}
