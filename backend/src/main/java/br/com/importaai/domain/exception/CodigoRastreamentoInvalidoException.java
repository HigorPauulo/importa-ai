package br.com.importaai.domain.exception;

public class CodigoRastreamentoInvalidoException extends RuntimeException {
    public CodigoRastreamentoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
