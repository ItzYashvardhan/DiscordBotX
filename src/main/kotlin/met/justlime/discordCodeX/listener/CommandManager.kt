package met.justlime.discordCodeX.listener

import met.justlime.discordCodeX.commands.JRedeemCode
import met.justlime.discordCodeX.commands.redeemcode.RCXDeleteCommand
import met.justlime.discordCodeX.commands.redeemcode.RCXGenCommand
import met.justlime.discordCodeX.commands.redeemcode.RCXModifyCommand
import met.justlime.discordCodeX.commands.redeemcode.RCXUsageCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CommandManager(private val jda: JDA, private val guilds: List<String>, private val roles: List<String>) : ListenerAdapter() {

    private val commands = mutableMapOf<String, JRedeemCode>()

    /**
     * Registers all commands at once.
     */
    private fun register(vararg commandList: JRedeemCode) {
        commandList.forEach { command ->
            val commandData = command.buildCommand()
            commands[commandData.name] = command
        }

        // Register commands with Discord
        val commandDataList = commands.values.map { it.buildCommand() }
        jda.guilds.forEach { guid ->
            guid.channels.forEach {
                val privilege = if (channel.id == allowedChannelId) {
                } else {
                    CommandPrivilege.disableChannel(channel.id) // Disable in other channels
                }

                guild.updateCommandPrivileges(command.id, privilege).queue()
            }

        }
        jda.updateCommands().addCommands(commandDataList).queue()

    }

    fun initializeCommands() {
        val commands = listOf(RCXGenCommand(), RCXDeleteCommand(), RCXModifyCommand(), RCXUsageCommand())
        register(*commands.toTypedArray())
    }

    /**
     * Handles command execution.
     */
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (!event.isFromGuild) return
        val member = event.member ?: return
        if (!guilds.contains(event.guild?.id)) {
            event.guild?.leave()?.queue()
            return
        }
        if (!member.roles.any { role -> roles.contains(role.id) }) {
            event.reply("You don't have permission to use this command").setEphemeral(true).queue()
            return
        }

        val command = commands[event.name]
        if (command != null) {
            command.execute(event)
        } else {
            event.reply("Unknown command!").setEphemeral(true).queue()
        }
    }

    /**
     * Handles auto-completion events.
     */
    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        if (!event.isFromGuild) return
        if (!guilds.contains(event.guild?.id)) {
            event.guild?.leave()?.queue()
            return
        }
        val member = event.member ?: return
        if (!member.roles.any { role -> roles.contains(role.id) }) {
            return
        }
        val command = commands[event.name]
        if (command != null) {
            val completions = command.handleAutoComplete(event)
            event.replyChoices(completions).queue()
        }
    }
}