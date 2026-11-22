package com.example.livraria.model.domain;

import com.example.livraria.model.enums.StatusLivro;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public abstract class Livro {
    protected String titulo;
    protected List<Categoria> categorias = new ArrayList<>();
    protected StatusLivro status;
    protected BigDecimal preco;
    protected Autor autor;
    protected String resumo;
    protected Integer numPag;
    protected BigDecimal desconto;
    protected String ISBN;
    protected Editora editora;

    public abstract BigDecimal getDesconto();
    public abstract void setEstoque(int estoque);

    public BigDecimal getPrecoComDesconto() {
        if (this.preco == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal desc = getDesconto();
        if (desc == null) desc = BigDecimal.ZERO;
        BigDecimal fator = BigDecimal.ONE.subtract(desc);
        return preco.multiply(fator);
    }

    public boolean isDisponivel() {
        return this.status == StatusLivro.DISPONIVEL;
    }

    // Getters e setters
    public StatusLivro getStatus() { return status; }
    public void setStatus(StatusLivro status) { this.status = status; }
    public String getTitulo() { return titulo; }
    public BigDecimal getPreco() { return preco; }
    public Autor getAutor() { return autor; }
    public Editora getEditora() { return editora; }
    public String getISBN() { return ISBN; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

}
