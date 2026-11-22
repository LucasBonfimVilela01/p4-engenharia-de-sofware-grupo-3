package com.example.livraria.controller;

import com.example.livraria.model.domain.*;
import com.example.livraria.model.enums.StatusLivro;
import com.example.livraria.model.strategy.*;
import com.example.livraria.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/checkout")
@SessionAttributes({"carrinho", "cliente"})
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private LivroService livroService;

    @ModelAttribute("carrinho")
    public Carrinho criarCarrinho() {
        return new Carrinho();
    }

    @ModelAttribute("cliente")
    public Cliente criarClienteExemplo() {
        Cliente cliente = new Cliente("Maria Gomes", "123.456.789-00", LocalDate.of(1995, 8, 20), "maria@email.com", "(11) 98888-7777");
        cliente.adicionarEndereco(new Endereco("Rua dos pinhais", "50", "Centro", "01001-000", "Apto 301"));
        return cliente;
    }

    @GetMapping
    public String exibirCheckout(@ModelAttribute("carrinho") Carrinho carrinho, Model model) {
        // Popula um livro de teste caso o carrinho esteja vazio para facilidade de demonstração
        if (carrinho.isVazio()) {
            Brochura livro = new Brochura();
            livro.setTitulo("Arquitetura de Software");
            livro.setPreco(new BigDecimal("50.00"));
            livro.setStatus(StatusLivro.DISPONIVEL);
            livro.setEstoque(5);

            carrinho.adicionarLivro(livro);
        }

        model.addAttribute("carrinho", carrinho);
        return "checkout";
    }

    @PostMapping("/efetuar")
    public String efetuarPedido(@RequestParam String tipoFrete,
                                @RequestParam String tipoPagamento,
                                @RequestParam(defaultValue = "1") int parcelas,
                                @RequestParam(defaultValue = "APROVADO") String statusSimulado,
                                @ModelAttribute("carrinho") Carrinho carrinho,
                                @ModelAttribute("cliente") Cliente cliente, Model model)
    { try {
        Fretavel frete = "EXPRESSA".equalsIgnoreCase(tipoFrete) ? new EntregaExpressa() : new EntregaNormal();
        Descontavel pagamento = "PIX".equalsIgnoreCase(tipoPagamento)
                ? new PIX()
                : new Cartao(parcelas == 1, parcelas);

        var cenario = PagamentoService.StatusSimulacao.valueOf(statusSimulado.toUpperCase());

        Pedido pedido = pedidoService.realizarPedido(cliente, carrinho, frete, pagamento, cenario);

        model.addAttribute("pedido", pedido);
        return "confirmacao";
    } catch (Exception e) {
        model.addAttribute("erro", e.getMessage());
        return "checkout";
    }
    }
}
