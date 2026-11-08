package com.loxstyleff.painel;

interface IShellService {
    // Método reservado pelo Shizuku para encerrar o serviço
    void destroy() = 16777114;

    // Executa um script de shell (identidade shell, via Shizuku) e devolve a saída
    String run(String script) = 1;
}
