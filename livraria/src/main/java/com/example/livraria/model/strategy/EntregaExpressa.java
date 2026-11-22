package com.example.livraria.model.strategy;

import java.math.BigDecimal;

public class EntregaExpressa implements Fretavel{
    private String statusEntrega = "PENDENTE";

    @Override public BigDecimal calcularFrete(String cep) {
        return new BigDecimal("30.00");
    }

    @Override
    public int getTempo() { return 2; }

    public String getStatusEntrega() {return statusEntrega;}
    public void setStatusEntrega(String statusEntrega) { this.statusEntrega = statusEntrega; }
}
