package com.example.livraria;

import com.example.livraria.model.domain.Brochura;
import com.example.livraria.model.domain.Carrinho;
import com.example.livraria.model.domain.Cliente;
import com.example.livraria.model.domain.Endereco;
import com.example.livraria.model.domain.Livro;
import com.example.livraria.model.domain.LivroFisico;
import com.example.livraria.model.domain.Pedido;
import com.example.livraria.model.enums.StatusLivro;
import com.example.livraria.model.enums.StatusPedido;
import com.example.livraria.model.strategy.EntregaNormal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Testes de sistema do caso de uso Efetuar Pedido (CSU02).
 *
 * Cada método implementa um caso de teste (CT) da planilha de testes da P4, na mesma ordem.
 * Os testes usam a mesma entrada que o cliente usa na tela (PedidoController, via MockMvc):
 * abrir o checkout (GET /checkout) e confirmar o pedido (POST /checkout/efetuar).
 *
 * Teste que passa  = CT com status OK.
 * Teste que falha  = CT com status NOK (defeito encontrado); a mensagem da falha descreve o defeito.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
class EfetuarPedidoTest {

    private static final BigDecimal DESCONTO_BROCHURA = new BigDecimal("0.95"); // RN02: brochura 5%
    private static final BigDecimal DESCONTO_CARTAO_A_VISTA = new BigDecimal("0.97"); // RN04: cartão à vista 3%
    private static final BigDecimal DESCONTO_PIX = new BigDecimal("0.92"); // RN04: Pix 8%

    @Autowired
    private MockMvc mvc;

    private MockHttpSession sessao;

    @BeforeEach
    void novaSessao() {
        sessao = new MockHttpSession();
    }

    // ------------------------------------------------------------------ fluxo principal e alternativos

    @Test
    @DisplayName("CT01 - Fluxo Principal (C1 / S3)")
    void ct01_fluxoPrincipal() throws Exception {
        Carrinho carrinho = abrirCheckout();
        Livro livro = carrinho.getLivros().get(0);
        int estoqueAntes = estoque(livro);

        // condições de entrada
        assertEquals(StatusLivro.DISPONIVEL, livro.getStatus()); // RN01
        assertInstanceOf(Brochura.class, livro);                // RN02
        assertTrue(estoqueAntes > 2);                           // RN03

        ModelAndView resposta = efetuar("CARTAO", 3, "APROVADO"); // RN04: parcelado em 3x

        // saída esperada
        Pedido pedido = pedido(resposta);
        assertEquals(StatusPedido.CONFIRMADO, pedido.getStatus());
        BigDecimal esperado = livro.getPreco().multiply(DESCONTO_BROCHURA).add(freteNormal()); // parcelado sem juros
        assertValor(esperado, pedido.getValorPedido());
        assertEquals(estoqueAntes - 1, estoque(livro), "O estoque deveria ser atualizado.");
        assertTrue(carrinho.isVazio(), "O carrinho deveria ser esvaziado.");
    }

    @Test
    @DisplayName("CT02 - Fluxo Alternativo 7b - Cartão à vista (C1 / S3)")
    void ct02_cartaoAVista() throws Exception {
        Livro livro = abrirCheckout().getLivros().get(0);

        ModelAndView resposta = efetuar("CARTAO", 1, "APROVADO"); // parcelas = 1 -> à vista

        Pedido pedido = pedido(resposta);
        assertEquals(StatusPedido.CONFIRMADO, pedido.getStatus());
        BigDecimal esperado = totalSemDescontoDePagamento(livro).multiply(DESCONTO_CARTAO_A_VISTA);
        assertValor(esperado, pedido.getValorPedido());
    }

    @Test
    @DisplayName("CT03 - Fluxo Alternativo 7a - Pix (C6 / S3)")
    void ct03_pix() throws Exception {
        Livro livro = abrirCheckout().getLivros().get(0);

        ModelAndView resposta = efetuar("PIX", 1, "APROVADO");

        Pedido pedido = pedido(resposta);
        assertEquals(StatusPedido.CONFIRMADO, pedido.getStatus());
        BigDecimal esperado = totalSemDescontoDePagamento(livro).multiply(DESCONTO_PIX);
        assertValor(esperado, pedido.getValorPedido());
    }

    @Test
    @DisplayName("CT04 - Fluxo Alternativo 5a - Cliente não autenticado (C3 / S3)")
    void ct04_clienteNaoAutenticado() throws Exception {
        // condição: nova sessão, o cliente não fez cadastro nem login
        abrirCheckout();

        ModelAndView resposta = efetuar("CARTAO", 3, "APROVADO");

        // esperado: o sistema pede cadastro/autenticação antes de continuar o pedido
        assertNotEquals("confirmacao", resposta.getViewName(),
                "Não existe cadastro nem autenticação: o sistema confirmou o pedido usando sempre o mesmo cliente de exemplo.");
    }

    @Test
    @DisplayName("CT05 - Fluxo Alternativo 4a - Livro indisponível (C4)")
    void ct05_livroIndisponivel() throws Exception {
        Livro livro = abrirCheckout().getLivros().get(0);
        int estoqueAntes = estoque(livro);
        livro.setStatus(StatusLivro.INDISPONIVEL); // condição: livro.status == indisponivel

        ModelAndView resposta = efetuar("CARTAO", 3, "APROVADO");

        // esperado: o sistema informa a indisponibilidade e não continua o pedido
        assertEquals("checkout", resposta.getViewName(),
                "O sistema não verificou a disponibilidade do livro (RN01) e confirmou o pedido.");
        assertNotNull(resposta.getModel().get("erro"));
        assertEquals(estoqueAntes, estoque(livro));
    }

    @Test
    @DisplayName("CT06 - Fluxo Principal - Estoque mínimo RN03 (C8 / S3)")
    void ct06_estoqueMinimo(CapturedOutput saida) throws Exception {
        // condição: o cliente compra o mesmo livro pela tela até o estoque chegar a 3 e então compra mais um
        // (o livro começa com 5 exemplares: 3 compras deixam 2 exemplares)
        Livro livro = null;
        for (int compra = 1; compra <= 3; compra++) {
            livro = abrirCheckout().getLivros().get(0);
            efetuar("CARTAO", 3, "APROVADO");
        }

        // esperado: estoque = 2 e funcionário notificado
        assertEquals(2, estoque(livro),
                "O estoque nunca chega a 2: a cada compra o checkout cria de novo o livro de exemplo com 5 exemplares.");
        assertTrue(saida.getOut().contains("[ALERTA DE ESTOQUE]"),
                "O funcionário não foi notificado de que o estoque chegou ao mínimo.");
    }

    // ------------------------------------------------------------------ fluxos de exceção

    @Test
    @DisplayName("CT07 - Fluxo de Exceção 2a - Carrinho vazio (C2)")
    void ct07_carrinhoVazio() throws Exception {
        // condição: o cliente abre o checkout sem ter adicionado livros (itens == 0)
        Carrinho carrinho = abrirCheckout();
        assertTrue(carrinho.isVazio(),
                "Ao abrir o checkout, o sistema colocou sozinho um livro no carrinho vazio.");

        ModelAndView resposta = efetuar("CARTAO", 3, "APROVADO");

        // esperado: mensagem de carrinho vazio e o pedido não continua
        assertEquals("checkout", resposta.getViewName());
        assertTrue(String.valueOf(resposta.getModel().get("erro")).contains("carrinho vazio"));
    }

    @Test
    @DisplayName("CT08 - Fluxo de Exceção 9a - Falha no cálculo do frete (C5 / S3)")
    void ct08_falhaNoFrete() throws Exception {
        // condição: o Sistema de Frete não consegue calcular o frete (CEP inexistente)
        Cliente cliente = new Cliente("Cliente Teste", "111.222.333-44", LocalDate.of(2000, 1, 1),
                "cliente@teste.com", "(11) 90000-0000");
        cliente.adicionarEndereco(new Endereco("Rua Teste", "1", "Centro", "00000-000", ""));
        sessao.setAttribute("cliente", cliente);
        abrirCheckout();

        ModelAndView resposta = efetuar("CARTAO", 3, "APROVADO");

        // esperado: o sistema informa a falha no frete e não finaliza o pedido
        assertEquals("checkout", resposta.getViewName(),
                "O frete tem valor fixo: o sistema não consulta o Sistema de Frete, não trata a falha e confirmou o pedido.");
        assertNotNull(resposta.getModel().get("erro"));
    }

    @Test
    @DisplayName("CT09 - Fluxo de Exceção 14a - Pagamento recusado e nova tentativa (C7 / S6)")
    void ct09_recusaENovaTentativa() throws Exception {
        Carrinho carrinho = abrirCheckout();
        Livro livro = carrinho.getLivros().get(0);
        int estoqueAntes = estoque(livro);

        // 1ª tentativa: pag == false
        Pedido recusado = pedido(efetuar("CARTAO", 3, "RECUSADO"));
        assertNotEquals(StatusPedido.CONFIRMADO, recusado.getStatus());
        assertEquals(estoqueAntes, estoque(livro), "O estoque não deveria ser atualizado após a recusa.");
        assertFalse(carrinho.isVazio(), "Os itens deveriam continuar no carrinho após a recusa.");

        // 2ª tentativa: pag == true
        Pedido confirmado = pedido(efetuar("CARTAO", 3, "APROVADO"));
        assertEquals(StatusPedido.CONFIRMADO, confirmado.getStatus());
        assertEquals(estoqueAntes - 1, estoque(livro));
        assertTrue(carrinho.isVazio());
    }

    @Test
    @DisplayName("CT10 - Fluxo de Exceção 14a - Pagamento recusado e cancelamento (trecho de C7 / S4)")
    void ct10_recusaECancelamento() throws Exception {
        Carrinho carrinho = abrirCheckout();
        Livro livro = carrinho.getLivros().get(0);
        int estoqueAntes = estoque(livro);

        Pedido pedido = pedido(efetuar("CARTAO", 3, "RECUSADO")); // pag == false

        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
        assertEquals(estoqueAntes, estoque(livro), "O estoque não deveria ser atualizado.");
        assertFalse(carrinho.isVazio(), "Os itens deveriam continuar no carrinho.");
    }

    @Test
    @DisplayName("CT11 - Fluxo de Exceção 14b - Pagamento sem resposta (trecho de C7 / trecho de S6)")
    void ct11_pagamentoSemResposta() throws Exception {
        Carrinho carrinho = abrirCheckout();
        Livro livro = carrinho.getLivros().get(0);
        int estoqueAntes = estoque(livro);

        Pedido pedido = pedido(efetuar("CARTAO", 3, "TIMEOUT")); // Sistema de Cartão sem resposta

        assertEquals(StatusPedido.EM_PROCESSAMENTO, pedido.getStatus());
        assertEquals(estoqueAntes, estoque(livro), "O estoque não deveria ser atualizado.");
        assertFalse(carrinho.isVazio(), "Os itens deveriam continuar no carrinho.");
    }

    // ------------------------------------------------------------------ status do pedido (diagrama de estados)

    @Test
    @DisplayName("CT12 - Fluxo de Exceção - Cancelamento pelo cliente (S1)")
    void ct12_cancelamentoPeloCliente() throws Exception {
        abrirCheckout();
        Pedido pedido = pedido(efetuar("CARTAO", 3, "TIMEOUT"));
        assertEquals(StatusPedido.EM_PROCESSAMENTO, pedido.getStatus());

        // condição: o cliente pede o cancelamento do pedido em processamento
        int http = mvc.perform(post("/checkout/cancelar").session(sessao)).andReturn().getResponse().getStatus();

        // esperado: o pedido passa para cancelado
        assertTrue(http < 400,
                "Não existe opção para o cliente cancelar o pedido (POST /checkout/cancelar respondeu HTTP " + http + ").");
        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
    }

    @Test
    @DisplayName("CT13 - Fluxo Principal - Status do pedido: em transporte e finalizado (C1 / S3)")
    void ct13_emTransporteEFinalizado() throws Exception {
        abrirCheckout();
        Pedido pedido = pedido(efetuar("CARTAO", 3, "APROVADO"));
        assertEquals(StatusPedido.CONFIRMADO, pedido.getStatus());
        EntregaNormal entrega = (EntregaNormal) pedido.getFrete();

        entrega.setStatusEntrega("em viagem");
        assertEquals(StatusPedido.EM_TRANSPORTE, pedido.getStatus(),
                "O pedido não passa para em transporte: não há mudança de status ligada à entrega.");

        entrega.setStatusEntrega("entregue");
        assertEquals(StatusPedido.FINALIZADO, pedido.getStatus());
    }

    @Test
    @DisplayName("CT14 - Fluxo de Exceção - Entrega inválida (C1 / S2)")
    void ct14_entregaInvalida() throws Exception {
        abrirCheckout();
        Pedido pedido = pedido(efetuar("CARTAO", 3, "APROVADO"));
        assertEquals(StatusPedido.CONFIRMADO, pedido.getStatus());
        EntregaNormal entrega = (EntregaNormal) pedido.getFrete();

        entrega.setStatusEntrega("em viagem");
        assertEquals(StatusPedido.EM_TRANSPORTE, pedido.getStatus(),
                "O pedido não passa para em transporte: não há controle da entrega depois da confirmação.");

        entrega.setStatusEntrega("inválida");
        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
    }

    @Test
    @DisplayName("CT15 - Fluxo de Exceção 14a - Recusa, nova tentativa e entrega inválida (C7 / S5)")
    void ct15_recusaNovaTentativaEEntregaInvalida() throws Exception {
        abrirCheckout();
        efetuar("CARTAO", 3, "RECUSADO");                         // 1ª tentativa: pag == false
        Pedido pedido = pedido(efetuar("CARTAO", 3, "APROVADO")); // 2ª tentativa: pag == true
        assertEquals(StatusPedido.CONFIRMADO, pedido.getStatus());
        EntregaNormal entrega = (EntregaNormal) pedido.getFrete();

        entrega.setStatusEntrega("em viagem");
        assertEquals(StatusPedido.EM_TRANSPORTE, pedido.getStatus(),
                "O pedido não passa para em transporte: não há controle da entrega depois da confirmação.");

        entrega.setStatusEntrega("inválida");
        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
    }

    // ------------------------------------------------------------------ apoio

    /** Abre a tela de checkout (GET /checkout) e devolve o carrinho guardado na sessão do cliente. */
    private Carrinho abrirCheckout() throws Exception {
        mvc.perform(get("/checkout").session(sessao));
        return (Carrinho) sessao.getAttribute("carrinho");
    }

    /** Confirma o pedido como na tela: frete normal, forma de pagamento, parcelas e resposta do sistema de pagamento. */
    private ModelAndView efetuar(String formaPagamento, int parcelas, String respostaPagamento) throws Exception {
        return mvc.perform(post("/checkout/efetuar").session(sessao)
                        .param("tipoFrete", "NORMAL")
                        .param("tipoPagamento", formaPagamento)
                        .param("parcelas", String.valueOf(parcelas))
                        .param("statusSimulado", respostaPagamento))
                .andReturn()
                .getModelAndView();
    }

    private static Pedido pedido(ModelAndView resposta) {
        Object pedido = resposta.getModel().get("pedido");
        assertNotNull(pedido, "O sistema não gerou o pedido (tela: " + resposta.getViewName()
                + ", erro: " + resposta.getModel().get("erro") + ").");
        return (Pedido) pedido;
    }

    private static int estoque(Livro livro) {
        return ((LivroFisico) livro).getEstoque();
    }

    private static BigDecimal freteNormal() {
        return new EntregaNormal().calcularFrete(null);
    }

    /** Preço do livro com o desconto do formato (RN02) + frete, antes do desconto da forma de pagamento (RN04). */
    private static BigDecimal totalSemDescontoDePagamento(Livro livro) {
        return livro.getPreco().multiply(DESCONTO_BROCHURA).add(freteNormal());
    }

    private static void assertValor(BigDecimal esperado, BigDecimal obtido) {
        assertNotNull(obtido, "O pedido não tem valor calculado.");
        assertEquals(0, esperado.compareTo(obtido),
                "Valor do pedido esperado " + esperado.stripTrailingZeros().toPlainString()
                        + ", obtido " + obtido.stripTrailingZeros().toPlainString());
    }
}
