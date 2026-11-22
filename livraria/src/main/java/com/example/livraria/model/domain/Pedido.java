package com.example.livraria.model.domain;

import com.example.livraria.model.enums.StatusPedido;
import com.example.livraria.model.strategy.Fretavel;
import java.math.BigDecimal;

public class Pedido {
    private Carrinho carrinho;
    private Cliente cliente;
    private StatusPedido status;
    private BigDecimal valorPedido; // Atributo derivado (- / valorPedido)
    private Fretavel frete;

    public Pedido(Cliente cliente, Carrinho carrinho, Fretavel frete) {
        this.cliente = cliente;
        this.carrinho = carrinho;
        this.frete = frete;
        this.status = StatusPedido.EM_PROCESSAMENTO;
    }

    public BigDecimal calcValorPedido() {
        BigDecimal totalLivros = (carrinho != null) ? carrinho.getValorCarrinho() : BigDecimal.ZERO;
        BigDecimal valorFrete = BigDecimal.ZERO;

        if (frete != null && cliente != null && !cliente.getEnderecos().isEmpty()) {
            String cep = cliente.getEnderecos().get(0).getCep();
            valorFrete = frete.calcularFrete(cep);
        }

        this.valorPedido = totalLivros.add(valorFrete);
        return this.valorPedido;
    }

    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }
    public BigDecimal getValorPedido() { return valorPedido; }
    public void setValorPedido(BigDecimal valorPedido) { this.valorPedido = valorPedido; }
    public Carrinho getCarrinho() { return carrinho; }
    public Cliente getCliente() { return cliente; }
    public Fretavel getFrete() { return frete; }
}
