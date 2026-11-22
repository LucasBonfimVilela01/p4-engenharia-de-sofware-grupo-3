package com.example.livraria.model.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Cartão de crédito: 3% de desconto e sem juros até 3x sem juros
public class Cartao implements Descontavel{
    private boolean aVista;
    private int parcelas;

    public Cartao(boolean aVista, int parcelas) {
        if (!aVista && (parcelas < 1 || parcelas > 3)) {
            throw new IllegalArgumentException("Parcelamento permitido em até 3 vezes sem juros.");
        }
        this.aVista = aVista;
        this.parcelas = parcelas;
    }

    @Override
    public BigDecimal aplicarDesconto(BigDecimal valorTotal) {
        if (aVista) {
            BigDecimal desconto = new BigDecimal("0.03");
            return valorTotal.multiply(BigDecimal.ONE.subtract(desconto));
        }
        return valorTotal;
    }

    public BigDecimal getValorParcela(BigDecimal valorTotal) {
        BigDecimal valorComDesconto = aplicarDesconto(valorTotal);
        return valorComDesconto.divide(BigDecimal.valueOf(parcelas), 2, RoundingMode.HALF_UP);
    }
}
