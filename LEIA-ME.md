# Painel by.Loxstyleff (Shizuku)

App nativo de Android com botões que rodam os comandos de otimização via Shizuku.

## Compilar

**Android Studio (computador):** abra esta pasta (File > Open), espere o Gradle sincronizar e use
Build > Build APK(s). O arquivo sai em `app/build/outputs/apk/debug/app-debug.apk`.

**GitHub Actions (sem instalar nada):** crie um repositório, envie todo o conteúdo desta pasta
(incluindo a pasta oculta `.github`), abra a aba Actions, rode "Gerar APK" e baixe o artefato
`painel-loxstyleff-apk` ao final.

## Usar

1. Instale e inicie o Shizuku (Depuração sem fio). Depois de reiniciar o celular, inicie de novo.
2. Instale o APK e abra o painel.
3. Toque em "Autorizar este painel" e aceite no Shizuku.
4. Use "Configuração inicial" uma vez, "Ativar otimização" antes de jogar e "Reverter" depois.

Para o Free Fire Max, marque a caixa "Uso o Free Fire Max" (afeta só a Manutenção).
