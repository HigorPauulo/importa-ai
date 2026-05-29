package br.com.importaai.domain.exception;

public class CotacaoIndisponivelException extends RuntimeException {

    public CotacaoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
