package com.example.livraria.service;

import com.example.livraria.model.domain.*;
import com.example.livraria.model.strategy.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PedidoService {

    @Autowired
    private LivroService livroService;

    @Autowired
    private PagamentoService pagamentoService;

    public Pedido realizarPedido(Cliente cliente, Carrinho carrinho, Fretavel modalidadeFrete, Descontavel formaPagamento, PagamentoService.StatusSimulacao cenarioPagamento) {
        // Exceção 2a: validação de carrinho vazio
        if (carrinho == null || carrinho.isVazio()) {
            throw new IllegalStateException("Não é possível fechar o pedido com o carrinho vazio.");
        }

        // Instancia o pedido e calcula o total(carrinho + frete)
        Pedido pedido = new Pedido(cliente, carrinho, modalidadeFrete);
        pedido.calcValorPedido();

        // Processa a confirmação do pagamento
        boolean pago = pagamentoService.processarPagamento(pedido, formaPagamento, cenarioPagamento);

        // Atualiza estoque (RN03) e esvazia o carrinho se for APROVADO
        if (pago) {
            for (Livro livro : carrinho.getLivros()) {
                livroService.atualizarEstoqueEStatus(livro, 1);
            }
            carrinho.limpar();
        }

        return pedido;
    }
}
