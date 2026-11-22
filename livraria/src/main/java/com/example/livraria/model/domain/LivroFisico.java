package com.example.livraria.model.domain;

import java.math.BigDecimal;

public abstract class LivroFisico extends Livro {
    protected Integer estoque;

    public Integer getEstoque() { return estoque; }

    @Override public void setEstoque(int estoque) {this.estoque = estoque; }
}
