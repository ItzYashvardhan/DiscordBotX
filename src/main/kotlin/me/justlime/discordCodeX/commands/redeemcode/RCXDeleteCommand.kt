package me.justlime.discordCodeX.commands.redeemcode

import api.justlime.redeemcodex.RedeemXAPI
import me.justlime.discordCodeX.commands.JRedeemCode
import me.justlime.discordCodeX.enums.JMessages
import me.justlime.discordCodeX.rxbPlugin
import me.justlime.discordCodeX.utils.JServices
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

class RCXDeleteCommand : JRedeemCode {

    override fun buildCommand(): CommandData {
        return Commands.slash(
            JServices.getMessage(JMessages.DELETE_COMMAND.path),
            JServices.getMessage(JMessages.DELETE_DESCRIPTION.path)
        ).addSubcommands(
            SubcommandData(
                JServices.getMessage(JMessages.DELETE_CODE_SUBCOMMAND.path),
                JServices.getMessage(JMessages.DELETE_CODE_DESCRIPTION.path)
            ).addOptions(
                OptionData(
                    OptionType.STRING,
                    JServices.getMessage(JMessages.DELETE_CODE_COMPLETION.path),
                    JServices.getMessage(JMessages.DELETE_CODE_OPTION_DESCRIPTION.path),
                    false
                ).setAutoComplete(true)
            ),
            SubcommandData(
                JServices.getMessage(JMessages.DELETE_TEMPLATE_SUBCOMMAND.path),
                JServices.getMessage(JMessages.DELETE_TEMPLATE_DESCRIPTION.path)
            ).addOptions(
                OptionData(
                    OptionType.STRING,
                    JServices.getMessage(JMessages.DELETE_TEMPLATE_COMPLETION.path),
                    JServices.getMessage(JMessages.DELETE_TEMPLATE_OPTION_DESCRIPTION.path),
                    false
                ).setAutoComplete(true)
            )
        ).setDefaultPermissions(DefaultMemberPermissions.DISABLED)
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        val subcommand = event.subcommandName
        val optionValue = when (subcommand) {
            JServices.getMessage(JMessages.DELETE_CODE_SUBCOMMAND.path) ->
                event.getOption(JServices.getMessage(JMessages.DELETE_CODE_COMPLETION.path))?.asString

            JServices.getMessage(JMessages.DELETE_TEMPLATE_SUBCOMMAND.path) ->
                event.getOption(JServices.getMessage(JMessages.DELETE_TEMPLATE_COMPLETION.path))?.asString

            else -> null
        }

        val result = when (subcommand) {
            JServices.getMessage(JMessages.DELETE_CODE_SUBCOMMAND.path) -> {
                optionValue
                    ?.split(" ")
                    ?.map(String::trim)
                    ?.filter { it.isNotEmpty() }
                    ?.map { RedeemXAPI.code.deleteCode(it) }
                    ?.any { it } ?: false
            }

            JServices.getMessage(JMessages.DELETE_TEMPLATE_SUBCOMMAND.path) -> {
                optionValue
                    ?.split(" ")
                    ?.map(String::trim)
                    ?.filter { it.isNotEmpty() }
                    ?.map { RedeemXAPI.template.deleteTemplate(it) }
                    ?.any { it } ?: false
            }

            else -> false
        }


        if (!result) {
            event.reply("Failed to delete $subcommand").setEphemeral(true).queue()
            return
        }

        val message = if (optionValue == "*") {
            "Deleted all $subcommand(s)."
        } else {
            "Deleted $subcommand: $optionValue"
        }

        event.reply("```\n$message```").queue()
    }

    override fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
        val focusedOption = event.focusedOption.name
        val fullInput = event.focusedOption.value
        val endsWithSpace = fullInput.endsWith(" ")
        val parts = fullInput.split(" ").map { it.trim() }.filter { it.isNotEmpty() }

        val alreadyEntered = parts.dropLast(if (endsWithSpace) 0 else 1).toSet()
        val query = if (endsWithSpace) "" else parts.lastOrNull()?.lowercase() ?: ""
        val prefix = if (endsWithSpace) fullInput.trim() else parts.dropLast(1).joinToString(" ")
        val maxChoices = 25

        return when (focusedOption) {
            JServices.getMessage(JMessages.DELETE_CODE_COMPLETION.path) -> {
                RedeemXAPI.code.getCodes()
                    .filter { code ->
                        val lower = code.lowercase()
                        lower.contains(query) && !alreadyEntered.contains(code)
                    }
                    .take(maxChoices)
                    .map { code ->
                        val suggestion = if (prefix.isNotEmpty()) "$prefix $code" else code
                        Command.Choice(suggestion, suggestion)
                    }
            }

            JServices.getMessage(JMessages.DELETE_TEMPLATE_COMPLETION.path) -> {
                RedeemXAPI.template.getTemplates()
                    .filter { template ->
                        val lower = template.lowercase()
                        lower.contains(query) && !alreadyEntered.contains(template)
                    }
                    .take(maxChoices)
                    .map { template ->
                        val suggestion = if (prefix.isNotEmpty()) "$prefix $template" else template
                        Command.Choice(suggestion, suggestion)
                    }
            }

            else -> emptyList()
        }
    }

}
