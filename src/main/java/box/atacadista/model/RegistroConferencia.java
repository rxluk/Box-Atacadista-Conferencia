package box.atacadista.model;

import java.time.LocalDate;

public class RegistroConferencia {

    private Long id;
    private LocalDate data;
    private String transacao;
    private String notaFiscal;
    private Conferente conferente;
    private Categoria tipo;

    public RegistroConferencia() {
    }

    public RegistroConferencia(String transacao, String notaFiscal, Conferente conferente, Categoria tipo) {
        this.data = LocalDate.now();
        this.id = null;
        this.transacao = transacao;
        this.notaFiscal = notaFiscal;
        this.conferente = conferente;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getTransacao() {
        return transacao;
    }

    public void setTransacao(String transacao) {
        this.transacao = transacao;
    }

    public String getNotaFiscal() {
        return notaFiscal;
    }

    public void setNotaFiscal(String notaFiscal) {
        this.notaFiscal = notaFiscal;
    }

    public Conferente getConferente() {
        return conferente;
    }

    public void setConferente(Conferente conferente) {
        this.conferente = conferente;
    }

    public Categoria getTipo() {
        return tipo;
    }

    public void setTipo(Categoria tipo) {
        this.tipo = tipo;
    }
}
