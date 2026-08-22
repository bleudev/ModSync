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
        builder.add("modsync.update.wait.running", "Updating mods")
        builder.add("modsync.update.wait.running.wait", "Please wait")
        builder.add("modsync.update.end.restart", "Restart Minecraft")
        builder.add("modsync.update.end.restart.more", "To connect to this server you need to restart the game")
    }
}

class ModSyncRuLanguageProvider(
    packOutput: FabricPackOutput, registryLookup: CompletableFuture<HolderLookup.Provider>
) : FabricLanguageProvider(packOutput, "ru_ru", registryLookup) {
    override fun generateTranslations(
        registryLookup: HolderLookup.Provider,
        builder: TranslationBuilder
    ) {
        builder.add("modsync.update.wait.running", "Обновление модов")
        builder.add("modsync.update.wait.running.wait", "Пожалуйста подождите")
        builder.add("modsync.update.end.restart", "Перезапустите Minecraft")
        builder.add("modsync.update.end.restart.more", "Чтобы присоединиться к этому серверу необходимо перезапустить игру")
    }
}