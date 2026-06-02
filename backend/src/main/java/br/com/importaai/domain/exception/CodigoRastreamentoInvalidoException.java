package br.com.importaai.domain.exception;

// Codigo de rastreamento informado pelo usuario nao tem formato plausivel (RF/entrada).
public class CodigoRastreamentoInvalidoException extends RuntimeException {
    public CodigoRastreamentoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
