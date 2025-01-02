package met.justlime.discordCodeX

import met.justlime.discordCodeX.commands.CommandManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import org.bukkit.plugin.java.JavaPlugin

class DiscordBotX : JavaPlugin() {
    private lateinit var jda: JDA
    private lateinit var commandManager: CommandManager

    override fun onEnable() {
        if(!this.dataFolder.exists()) this.dataFolder.mkdirs()
        saveDefaultConfig()
        // Plugin startup logic
        if (!config.getBoolean("bot.enabled")) {
            logger.warning("Bot is disabled in the config.yml. Disabling plugin.")
            return server.pluginManager.disablePlugin(this)
        }
        val token = config.getString("bot.token") ?: ""
        // Initialize JDA
        jda = BotManager.buildBot(token)

        commandManager = CommandManager(jda)
        commandManager.initializeCommands()
        // Command setup handled elsewhere

        // Register event listeners
        jda.addEventListener(commandManager)
    }

    override fun onDisable() {
        BotManager.shutdownBot()
        // Plugin shutdown logic
    }
}


