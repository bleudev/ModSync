package com.bleudev.modsync.datagen

import com.bleudev.modsync.datagen.provider.ModSyncDefaultLanguageProvider
import com.bleudev.modsync.datagen.provider.ModSyncRuLanguageProvider
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

class ModSyncDataGen : DataGeneratorEntrypoint {

    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        // Language
        pack.addProvider(::ModSyncDefaultLanguageProvider)
        pack.addProvider(::ModSyncRuLanguageProvider)
    }
}
