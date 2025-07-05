package me.justlime.discordCodeX.commands.configuration

import me.justlime.discordCodeX.enums.JFiles
import me.justlime.discordCodeX.enums.JMessages
import me.justlime.discordCodeX.rxbPlugin
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ConfigManager {
    lateinit var configuration: FileConfiguration
    lateinit var messages: FileConfiguration

    init {
        loadMessageConfig()
    }

    fun loadMessageConfig() {
        this.configuration = rxbPlugin.config
        val file = getFile(JFiles.MESSAGES.fileName)
        val config = YamlConfiguration.loadConfiguration(file)
        JMessages.entries.forEach {
            if (config.getString(it.path) == null) {
                config.set(it.path, it.path)
            }
        }
        config.save(file)
        this.messages = config
    }

    private fun getFile(jFiles: String): File {
        val file = File(rxbPlugin.dataFolder.path, jFiles)
        if (!file.exists()) {
            rxbPlugin.saveResource(jFiles, false)
        }
        return file
    }

    fun reload(){
        loadMessageConfig()
    }


}