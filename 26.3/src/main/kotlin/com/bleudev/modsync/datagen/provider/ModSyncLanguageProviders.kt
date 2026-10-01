package com.bleudev.modsync.datagen.provider

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.core.HolderLookup
import java.util.concurrent.CompletableFuture

class ModSyncDefaultLanguageProvider(
    packOutput: FabricPackOutput, registryLookup: CompletableFuture<HolderLookup.Provider>
) : FabricLanguageProvider(packOutput, registryLookup) {
    override fun generateTranslations(
        registryLookup: HolderLookup.Provider,
        builder: TranslationBuilder
    ) {
        builder.add("modsync.update.confirm.title", "Confirm installation")
        builder.add("modsync.update.confirm.message", "These mods will be updated.\nAre you sure you want to install them?")
        builder.add("modsync.update.confirm.yes", "Confirm, join the server")
        builder.add("modsync.update.confirm.no", "No, do not join the server")
        builder.add("modsync.update.updating.title", "Updating mods")
        builder.add("modsync.update.updating.message.top", "Updating ")
        builder.add("modsync.update.updating.message.wait", "Please wait")
        builder.add("modsync.update.end.restart", "Restart Minecraft")
        builder.add("modsync.update.end.restart.more", "To join this server, you must restart game")
        builder.add("modsync.update.end.restart.yes", "Restart")
        builder.add("modsync.update.end.restart.no", "Later")
    }
}

class ModSyncRuLanguageProvider(
    packOutput: FabricPackOutput, registryLookup: CompletableFuture<HolderLookup.Provider>
) : FabricLanguageProvider(packOutput, "ru_ru", registryLookup) {
    override fun generateTranslations(
        registryLookup: HolderLookup.Provider,
        builder: TranslationBuilder
    ) {
        builder.add("modsync.update.confirm.title", "Подтвердите установку")
        builder.add("modsync.update.confirm.message", "Эти моды будут обновлены\nВы точно хотите их установить?")
        builder.add("modsync.update.confirm.yes", "Подтвердить, зайти на сервер")
        builder.add("modsync.update.confirm.no", "Нет, не заходить на сервер")
        builder.add("modsync.update.updating.title", "Обновление модов")
        builder.add("modsync.update.updating.message.top", "Обновляем ")
        builder.add("modsync.update.updating.message.wait", "Пожалуйста подождите")
        builder.add("modsync.update.end.restart", "Перезапустите Minecraft")
        builder.add("modsync.update.end.restart.more", "Чтобы присоединиться к этому серверу необходимо перезапустить игру")
        builder.add("modsync.update.end.restart.yes", "Перезапустить")
        builder.add("modsync.update.end.restart.no", "Позже")
    }
}