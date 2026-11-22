package com.example.livraria.service;

import com.example.livraria.model.domain.Pedido;
import com.example.livraria.model.enums.StatusPedido;
import com.example.livraria.model.strategy.Descontavel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PagamentoService {

    public enum StatusSimulacao {APROVADO, RECUSADO, TIMEOUT}

    public boolean processarPagamento(Pedido pedido, Descontavel formaPagamento, StatusSimulacao resultadoSimulado) {
        // aplica o desconto de pagamento sobre o valor total do pedido (RN04)
        BigDecimal valorComDesconto = formaPagamento.aplicarDesconto(pedido.getValorPedido());

        //atribuição do novo valor ao pedido
        pedido.setValorPedido(valorComDesconto);

        switch (resultadoSimulado) {
            case APROVADO:
                pedido.setStatus(StatusPedido.CONFIRMADO);
                return true;

            case RECUSADO:
                // Exceção 14a: Transação recusada -> Pedido cancelado, estoque e carrinho intocados
                pedido.setStatus(StatusPedido.CANCELADO);
                return false;
            case TIMEOUT:
                default:
                    // Exceção 14b: Timeout -> Pedido permanece em processamento
                    pedido.setStatus(StatusPedido.EM_PROCESSAMENTO);
                    return false;
        }
    }
}
