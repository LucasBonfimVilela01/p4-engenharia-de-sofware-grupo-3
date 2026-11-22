package com.example.livraria.model.domain;

import java.math.BigDecimal;

// Digital: 10% de desconto
public class Digital extends Livro {
    @Override
    public BigDecimal getDesconto() {
        return new BigDecimal("0.10");
    }

    @Override
    public void
    setEstoque(int estoque) { }
}
