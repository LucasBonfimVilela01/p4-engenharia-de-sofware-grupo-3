package com.example.livraria.service;

import com.example.livraria.model.domain.Livro;
import com.example.livraria.model.domain.LivroFisico;
import com.example.livraria.model.enums.StatusLivro;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LivroService {

    // Repositório simulado em memória
    private final List<Livro> repositorioLivros = new ArrayList<>();

    public List<Livro> listarTodos() {
        return repositorioLivros;
    }

    public Optional<Livro> buscarPorIsbn(String isbn) {
        return repositorioLivros.stream()
                .filter(livro -> livro.getISBN().equalsIgnoreCase(isbn))
                .findFirst();
    }

    public void atualizarEstoqueEStatus (Livro livro, int quantidadeComprada) {
        if (livro instanceof LivroFisico livroFisico) {
            int novoEstoque = livroFisico.getEstoque() - quantidadeComprada;
            livroFisico.setEstoque(novoEstoque);

            // RN03: Alerta quando o estoque atingir 2 ou menos
            if (novoEstoque <= 2 && livroFisico.getStatus() == StatusLivro.DISPONIVEL) {
                System.out.println("[ALERTA DE ESTOQUE] O livro " + livroFisico.getTitulo() + " restam apenas" + novoEstoque + " unidade(s).");
            }

            if (novoEstoque <= 0) {
                livroFisico.setStatus(StatusLivro.INDISPONIVEL);
            }
        }
    }
}
