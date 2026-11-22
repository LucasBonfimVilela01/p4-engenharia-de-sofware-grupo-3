package com.example.livraria.model.domain;

public class Editora {
    private String nome;
    private String CNPJ;
    private String tel;
    private String email;

    public Editora(String nome, String CNPJ, String tel, String email) {
        this.nome = nome;
        this.CNPJ = CNPJ;
        this.tel = tel;
        this.email = email;
    }

    public String getNome() { return nome; }
    public String getCNPJ() { return CNPJ; }
    public String getTel() { return tel; }
    public String getEmail() { return email; }
}
