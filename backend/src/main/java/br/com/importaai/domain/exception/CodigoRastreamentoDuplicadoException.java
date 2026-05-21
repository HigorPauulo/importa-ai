package br.com.importaai.domain.exception;

public class CodigoRastreamentoDuplicadoException extends RuntimeException {
    public CodigoRastreamentoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
