package com.example.livraria.model.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Carrinho {
    private List<Livro> livros = new ArrayList<>();
    private BigDecimal valorCarrinho = BigDecimal.ZERO;

    public void adicionarLivro(Livro livro) {
        if (livro.getStatus() != com.example.livraria.model.enums.StatusLivro.DISPONIVEL) {
            throw new IllegalArgumentException("Livro indisponível: " + livro.getTitulo());
        }
        this.livros.add(livro);
        atualizarValorCarrinho();
    }

    private void atualizarValorCarrinho() {
        this.valorCarrinho = livros.stream()
                .map(Livro::getPrecoComDesconto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularValorDescontos() {
        return livros.stream()
                .map(livro -> livro.getPreco().subtract(livro.getPrecoComDesconto()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Livro> getLivros() {
        return livros;
    }

    public BigDecimal getValorCarrinho() {
        return valorCarrinho;
    }

    public boolean isVazio() {
        return livros.isEmpty();
    }

    public void limpar() {
        livros.clear();
        valorCarrinho = BigDecimal.ZERO;
    }
}
