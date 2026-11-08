package com.loxstyleff.painel

object Comandos {

    // Antes de jogar
    val ATIVAR = """
        am kill-all
        am force-stop com.android.chrome
        settings put global auto_sync 0
        settings put global wifi_scan_always_enabled 0
        settings put global ble_scan_always_enabled 0
        cmd notification set_dnd priority
    """.trimIndent()

    // Depois de jogar
    val REVERTER = """
        settings put global auto_sync 1
        settings put global wifi_scan_always_enabled 1
        settings put global ble_scan_always_enabled 1
        cmd notification set_dnd off
    """.trimIndent()

    // Uma vez só (fica salvo mesmo depois de reiniciar)
    val CONFIG_INICIAL = """
        cmd appops set com.zhiliaoapp.musically RUN_IN_BACKGROUND ignore
        cmd appops set com.zhiliaoapp.musically RUN_ANY_IN_BACKGROUND ignore
        cmd appops set com.instagram.android RUN_IN_BACKGROUND ignore
        cmd appops set com.instagram.android RUN_ANY_IN_BACKGROUND ignore
        cmd appops set com.google.android.youtube RUN_IN_BACKGROUND ignore
        cmd appops set com.google.android.youtube RUN_ANY_IN_BACKGROUND ignore
        pm disable-user --user 0 com.miui.analytics
        pm disable-user --user 0 com.miui.msa.global
        pm disable-user --user 0 com.xiaomi.discover
        pm disable-user --user 0 com.miui.bugreport
        pm disable-user --user 0 com.xiaomi.mipicks
        settings put global window_animation_scale 0
        settings put global transition_animation_scale 0
        settings put global animator_duration_scale 0
        settings put system min_refresh_rate 90.0
        settings put system peak_refresh_rate 90.0
        settings put global low_power 0
    """.trimIndent()

    // De vez em quando
    fun manutencao(pacote: String): String =
        "pm trim-caches 999G\ncmd package compile -m speed -f $pacote"
}
