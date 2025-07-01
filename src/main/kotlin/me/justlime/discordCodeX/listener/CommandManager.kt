package me.justlime.discordCodeX.listener

import me.justlime.discordCodeX.commands.JRedeemCode
import me.justlime.discordCodeX.commands.redeemcode.RCXDeleteCommand
import me.justlime.discordCodeX.commands.redeemcode.RCXGenCommand
import me.justlime.discordCodeX.commands.redeemcode.RCXModifyCommand
import me.justlime.discordCodeX.commands.redeemcode.RCXUsageCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CommandManager(
    private val jda: JDA,
    private val guilds: List<String>,
    private val roles: List<String>
) : ListenerAdapter() {

    private val commands = mutableMapOf<String, JRedeemCode>()

    /**
     * Registers all commands at once.
     */
    private fun register(vararg commandList: JRedeemCode) {
        commandList.forEach { command ->
            val commandData = command.buildCommand()
            commands[commandData.name] = command
        }

        val commandDataList = commands.values.map { it.buildCommand() }

        jda.guilds.forEach { guild ->
            guild.updateCommands().addCommands(commandDataList).queue { registeredCommands ->
                // You may apply privileges here if needed
                // Example: restrict command execution to roles
                // Note: Privileges are now handled via ApplicationCommandPermissions (JDA 5+)
                /*
                registeredCommands.forEach { cmd ->
                    val privileges = roles.map {
                        CommandPrivilege.enableRole(it)
                    }
                    guild.updateCommandPrivilegesById(cmd.id, privileges).queue()
                }
                */
            }
        }
    }

    fun initializeCommands() {
        val commands = listOf(
            RCXGenCommand(),
            RCXDeleteCommand(),
            RCXModifyCommand(),
            RCXUsageCommand()
        )
        register(*commands.toTypedArray())
    }

    /**
     * Handles command execution.
     */
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (!event.isFromGuild) return
        val guildId = event.guild?.id ?: return

        if (!guilds.contains(guildId)) {
            event.guild?.leave()?.queue()
            return
        }

        val member = event.member ?: return
        if (!member.roles.any { it.id in roles }) {
            event.reply("You don't have permission to use this command.").setEphemeral(true).queue()
            return
        }

        commands[event.name]?.execute(event)
            ?: event.reply("Unknown command!").setEphemeral(true).queue()
    }

    /**
     * Handles auto-completion events.
     */
    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        if (!event.isFromGuild) return
        val guildId = event.guild?.id ?: return
        if (!guilds.contains(guildId)) {
            event.guild?.leave()?.queue()
            return
        }

        val member = event.member ?: return
        if (!member.roles.any { it.id in roles }) return

        val command = commands[event.name]
        if (command != null) {
            val completions = command.handleAutoComplete(event)
            event.replyChoices(completions).queue()
        }
    }
}
