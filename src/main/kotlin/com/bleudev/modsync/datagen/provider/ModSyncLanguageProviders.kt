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
        builder.add("modsync.update.approve.title", "Approve installation")
        builder.add("modsync.update.approve.message", "These mods will be updated\nAre you sure to install them?")
        builder.add("modsync.update.approve.yes", "Approve, join the server")
        builder.add("modsync.update.approve.no", "No, don't join the server")
        builder.add("modsync.update.wait.running", "Updating mods")
        builder.add("modsync.update.wait.running.wait", "Please wait")
        builder.add("modsync.update.end.restart", "Restart Minecraft")
        builder.add("modsync.update.end.restart.more", "To connect to this server you need to restart the game")
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
        builder.add("modsync.update.approve.title", "Подтвердите установку")
        builder.add("modsync.update.approve.message", "Эти моды будут обновлены\nВы точно хотите их установить?")
        builder.add("modsync.update.approve.yes", "Подтвердить, зайти на сервер")
        builder.add("modsync.update.approve.no", "Нет, не заходить на сервер")
        builder.add("modsync.update.wait.running", "Обновление модов")
        builder.add("modsync.update.wait.running.wait", "Пожалуйста подождите")
        builder.add("modsync.update.end.restart", "Перезапустите Minecraft")
        builder.add("modsync.update.end.restart.more", "Чтобы присоединиться к этому серверу необходимо перезапустить игру")
        builder.add("modsync.update.end.restart.yes", "Перезапустить")
        builder.add("modsync.update.end.restart.no", "Потом")
    }
}