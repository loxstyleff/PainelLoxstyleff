package com.loxstyleff.painel

import android.app.Activity
import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import rikka.shizuku.Shizuku

class MainActivity : Activity() {

    private val laranja = Color.parseColor("#FF7A1A")
    private val fundo = Color.parseColor("#0D0F14")
    private val cartao = Color.parseColor("#151922")
    private val mudo = Color.parseColor("#8B94A7")

    private lateinit var status: TextView
    private lateinit var saida: TextView
    private lateinit var btnAbrir: Button
    private lateinit var btnAutorizar: Button
    private val botoesAcao = mutableListOf<Button>()

    private var servico: IShellService? = null
    private var vinculando = false
    private var ocupado = false

    private val prefs by lazy { getSharedPreferences("painel", MODE_PRIVATE) }

    private fun pacote(): String =
        if (prefs.getBoolean("max", false)) "com.dts.freefiremax" else "com.dts.freefireth"

    private val argsServico by lazy {
        Shizuku.UserServiceArgs(ComponentName(packageName, ShellService::class.java.name))
            .daemon(false)
            .processNameSuffix("shell")
            .debuggable(false)
            .version(1)
    }

    private val conexao = object : ServiceConnection {
        override fun onServiceConnected(nome: ComponentName?, binder: IBinder?) {
            vinculando = false
            if (binder != null && binder.pingBinder()) {
                servico = IShellService.Stub.asInterface(binder)
            }
            atualizarEstado()
        }

        override fun onServiceDisconnected(nome: ComponentName?) {
            servico = null
            vinculando = false
            atualizarEstado()
        }
    }

    private val aoReceberBinder = Shizuku.OnBinderReceivedListener {
        runOnUiThread { atualizarEstado() }
    }

    private val aoMorrerBinder = Shizuku.OnBinderDeadListener {
        runOnUiThread {
            servico = null
            vinculando = false
            atualizarEstado()
        }
    }

    private val aoResponderPermissao = Shizuku.OnRequestPermissionResultListener { _, _ ->
        runOnUiThread { atualizarEstado() }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun botao(texto: String, destaque: Boolean, acao: () -> Unit): Button {
        val b = Button(this)
        b.text = texto
        b.transformationMethod = null
        b.textSize = 17f
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        b.setTextColor(if (destaque) Color.parseColor("#1A0D00") else Color.WHITE)
        b.setBackgroundColor(if (destaque) laranja else cartao)
        b.setOnClickListener { acao() }
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(56))
        lp.topMargin = dp(10)
        b.layoutParams = lp
        return b
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val raiz = ScrollView(this)
        raiz.setBackgroundColor(fundo)
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(20), dp(40), dp(20), dp(40))
        raiz.addView(col)

        val assinatura = TextView(this)
        assinatura.text = "by.Loxstyleff"
        assinatura.textSize = 38f
        assinatura.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        assinatura.setTextColor(laranja)
        assinatura.setShadowLayer(24f, 0f, 0f, Color.parseColor("#66FF7A1A"))
        col.addView(assinatura)

        val sub = TextView(this)
        sub.text = "Painel de otimização para Free Fire no Redmi 13C"
        sub.setTextColor(mudo)
        sub.textSize = 15f
        sub.setPadding(0, dp(8), 0, dp(20))
        col.addView(sub)

        status = TextView(this)
        status.setTextColor(Color.WHITE)
        status.textSize = 15f
        col.addView(status)

        btnAbrir = botao("Abrir o Shizuku", false) { abrirShizuku() }
        btnAutorizar = botao("Autorizar este painel", true) { pedirPermissao() }
        col.addView(btnAbrir)
        col.addView(btnAutorizar)

        botoesAcao += botao("Ativar otimização", true) { executar("Ativar otimização", Comandos.ATIVAR) }
        botoesAcao += botao("Reverter depois de jogar", false) { executar("Reverter", Comandos.REVERTER) }
        botoesAcao += botao("Configuração inicial (uma vez só)", false) { executar("Configuração inicial", Comandos.CONFIG_INICIAL) }
        botoesAcao += botao("Manutenção (cache e compilar o jogo)", false) { executar("Manutenção", Comandos.manutencao(pacote())) }
        botoesAcao.forEach { col.addView(it) }

        val max = CheckBox(this)
        max.text = "Uso o Free Fire Max"
        max.setTextColor(mudo)
        max.isChecked = prefs.getBoolean("max", false)
        max.setOnCheckedChangeListener { _, marcado ->
            prefs.edit().putBoolean("max", marcado).apply()
        }
        val lpMax = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lpMax.topMargin = dp(14)
        col.addView(max, lpMax)

        saida = TextView(this)
        saida.typeface = Typeface.MONOSPACE
        saida.textSize = 12f
        saida.setTextColor(Color.parseColor("#C9D3E6"))
        saida.setBackgroundColor(cartao)
        saida.setPadding(dp(12), dp(12), dp(12), dp(12))
        saida.setTextIsSelectable(true)
        saida.text = "A saída dos comandos aparece aqui."
        val lpSaida = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lpSaida.topMargin = dp(16)
        col.addView(saida, lpSaida)

        setContentView(raiz)

        Shizuku.addBinderReceivedListenerSticky(aoReceberBinder)
        Shizuku.addBinderDeadListener(aoMorrerBinder)
        Shizuku.addRequestPermissionResultListener(aoResponderPermissao)

        atualizarEstado()
    }

    override fun onResume() {
        super.onResume()
        atualizarEstado()
    }

    override fun onDestroy() {
        Shizuku.removeBinderReceivedListener(aoReceberBinder)
        Shizuku.removeBinderDeadListener(aoMorrerBinder)
        Shizuku.removeRequestPermissionResultListener(aoResponderPermissao)
        try {
            if (servico != null) Shizuku.unbindUserService(argsServico, conexao, true)
        } catch (e: Throwable) {
            // sem ação: o Shizuku pode já ter encerrado
        }
        super.onDestroy()
    }

    private fun abrirShizuku() {
        val intent = packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
        if (intent != null) {
            startActivity(intent)
        } else {
            saida.text = "Shizuku não encontrado. Instale o Shizuku pela Play Store."
        }
    }

    private fun pedirPermissao() {
        try {
            Shizuku.requestPermission(1)
        } catch (e: Throwable) {
            saida.text = "Não foi possível pedir a permissão: ${e.message}"
        }
    }

    private fun atualizarBotoes() {
        val pronto = servico != null && !ocupado
        botoesAcao.forEach {
            it.isEnabled = pronto
            it.alpha = if (pronto) 1f else 0.4f
        }
    }

    private fun atualizarEstado() {
        val rodando = try { Shizuku.pingBinder() } catch (e: Throwable) { false }
        var autorizado = false
        if (rodando) {
            autorizado = try {
                !Shizuku.isPreV11() &&
                    Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            } catch (e: Throwable) {
                false
            }
        }

        if (!rodando) {
            status.text = "O Shizuku não está rodando. Abra o Shizuku e inicie pela Depuração sem fio."
        } else if (!autorizado) {
            status.text = "Falta autorizar este painel no Shizuku."
        } else if (servico == null) {
            status.text = "Conectando ao Shizuku..."
        } else {
            status.text = "Pronto. O painel está autorizado."
        }

        btnAbrir.visibility = if (rodando) View.GONE else View.VISIBLE
        btnAutorizar.visibility = if (rodando && !autorizado) View.VISIBLE else View.GONE

        if (rodando && autorizado && servico == null && !vinculando) {
            vinculando = true
            try {
                Shizuku.bindUserService(argsServico, conexao)
            } catch (e: Throwable) {
                vinculando = false
                saida.text = "Erro ao conectar: ${e.message}"
            }
        }
        atualizarBotoes()
    }

    private fun executar(titulo: String, script: String) {
        val s = servico
        if (s == null) {
            saida.text = "Ainda sem conexão com o Shizuku."
            return
        }
        ocupado = true
        atualizarBotoes()
        saida.text = "$titulo..."
        Thread {
            val resultado = try {
                s.run(script)
            } catch (e: Exception) {
                "Erro: ${e.message}"
            }
            runOnUiThread {
                ocupado = false
                atualizarBotoes()
                saida.text = "$titulo: concluído.\n\n" + resultado.trim()
            }
        }.start()
    }
}
