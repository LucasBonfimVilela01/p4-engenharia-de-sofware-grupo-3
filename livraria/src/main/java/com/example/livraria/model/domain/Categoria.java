package com.example.livraria.model.domain;

public class Categoria {
    private String nome;
    private String resumo;

    public Categoria(String nome, String resumo) {
        this.nome = nome;
        this.resumo = resumo;
    }

    public String getNome() { return nome; }
    public String getResumo() { return resumo; }
}
