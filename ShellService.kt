package com.loxstyleff.painel

import android.content.Context
import kotlin.system.exitProcess

// Roda no processo do Shizuku, com permissão de shell.
class ShellService() : IShellService.Stub() {

    // O Shizuku 13+ tenta primeiro o construtor com Context.
    constructor(@Suppress("UNUSED_PARAMETER") context: Context) : this()

    override fun destroy() {
        exitProcess(0)
    }

    override fun run(script: String): String {
        return try {
            val processo = ProcessBuilder("sh", "-c", script)
                .redirectErrorStream(true)
                .start()
            val saida = processo.inputStream.bufferedReader().readText()
            processo.waitFor()
            saida
        } catch (e: Exception) {
            "Erro: ${e.message}"
        }
    }
}
