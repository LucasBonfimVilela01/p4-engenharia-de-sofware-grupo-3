package com.example.livraria.model.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class Cliente {
    private String nome;
    private String CPF;
    private LocalDate dataNasc;
    private String email;
    private String tel;
    private List<Endereco> enderecos = new ArrayList<>();

    public Cliente() {}

    public Cliente(String nome, String CPF, LocalDate dataNasc, String email, String tel) {
        this.nome = nome;
        this.CPF = CPF;
        this.dataNasc = dataNasc;
        this.email = email;
        this.tel = tel;
    }

    public void adicionarEndereco(Endereco endereco) {
        this.enderecos.add(endereco);
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCPF() { return CPF; }
    public void setCPF(String CPF) { this.CPF = CPF; }
    public LocalDate getDataNasc() { return dataNasc; }
    public void setDataNasc(LocalDate dataNasc) { this.dataNasc = dataNasc; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }
    public List<Endereco> getEnderecos() { return enderecos; }
}