package me.justlime.discordCodeX

import me.justlime.discordCodeX.commands.configuration.ConfigManager
import me.justlime.discordCodeX.listener.CommandManager
import me.justlime.discordCodeX.listener.GuildJoinListener
import me.justlime.discordCodeX.utils.JServices
import net.dv8tion.jda.api.JDA
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

lateinit var rxbPlugin: RedeemXBot
class RedeemXBot : JavaPlugin() {
    private lateinit var jda: JDA

    override fun onEnable() {
        setupConfig()
        rxbPlugin = this

        JServices.configManager = ConfigManager()

        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) JServices.isPlaceholderHooked = true

        // Check if the bot is enabled
        if (!config.getBoolean("bot.enabled")) {
            logger.warning("Bot is disabled in the config.yml. Disabling plugin.")
            return disablePlugin()
        }

        // Retrieve bot token and guilds
        val token = config.getString("bot.token")?.takeIf { it.isNotEmpty() }
        val guilds = config.getStringList("guilds").takeIf { it.isNotEmpty() }
        val roles = config.getStringList("roles").takeIf { it.isNotEmpty() }

        if (token == null) {
            logger.severe("Bot token is missing in the config.yml. Disabling plugin.")
            return disablePlugin()
        }

        if (guilds == null) {
            logger.severe("Guilds are missing in the config.yml. Disabling plugin.")
            return disablePlugin()
        }

        if (roles == null) {
            logger.severe("Roles are missing in the config.yml. Disabling plugin.")
            return disablePlugin()
        }

        // Initialize JDA
        try {
            jda = BotManager.buildBot(token).apply { awaitReady() }
            jda.guilds.forEach { guild ->
                if (guild.id !in guilds) {
                    logger.warning("Leaving unauthorized guild: ${guild.name} (${guild.id})")
                    guild.leave().queue()
                }
            }
            logger.info("Bot connected successfully.")

            // Initialize commands and register listeners
            val commandManager = CommandManager(jda, guilds, roles)
            commandManager.initializeCommands()

            jda.addEventListener(commandManager)
            jda.addEventListener(GuildJoinListener(guilds))
            return
        } catch (exception: Exception) {
            logger.severe("Failed to initialize the Discord bot: ${exception.message}")
            exception.printStackTrace()
            return disablePlugin()
        }
    }

    override fun onDisable() {
        if (::jda.isInitialized) {
            BotManager.shutdownBot()
            logger.info("Bot has been shut down.")
        }
    }

    private fun setupConfig() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        saveDefaultConfig()
    }

    private fun disablePlugin() {
        server.pluginManager.disablePlugin(this)
    }
}
