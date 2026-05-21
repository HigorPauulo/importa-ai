package br.com.importaai.domain.model;

public enum Moeda {
    BRL("R$"),
    USD("$"),
    EUR("€"),
    CNY("¥");

    private final String simbolo;

    Moeda(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getSimbolo() {
        return simbolo;
    }
}
