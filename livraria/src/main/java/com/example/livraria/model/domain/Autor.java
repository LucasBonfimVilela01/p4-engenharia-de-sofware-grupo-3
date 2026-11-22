package com.example.livraria.model.domain;

import java.time.LocalDate;

public class Autor {
    private String nome;
    private LocalDate dataNasc;
    private String nacionalidade;

    public Autor(String nome, LocalDate dataNasc, String nacionalidade) {
        this.nome = nome;
        this.dataNasc = dataNasc;
        this.nacionalidade = nacionalidade;
    }

    public String getNome() {return nome;}
    public LocalDate getDataNasc() {return dataNasc;}
    public String getNacionalidade() { return nacionalidade; }
}
