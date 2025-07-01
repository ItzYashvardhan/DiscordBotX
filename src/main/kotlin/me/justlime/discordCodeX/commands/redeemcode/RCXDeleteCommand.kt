package me.justlime.discordCodeX.commands.redeemcode

import api.justlime.redeemcodex.RedeemXAPI
import me.justlime.discordCodeX.commands.JRedeemCode
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
        return Commands.slash("delete", "Delete codes or templates").addSubcommands(
                SubcommandData("code", "Delete a specific code or multiple codes").addOptions(
                        OptionData(
                            OptionType.STRING,
                            "code",
                            "The code to delete or '*' to delete all codes",
                            false
                        ).setAutoComplete(true), // Enable autocomplete for codes
                    ), SubcommandData("template", "Delete a specific template or reset all templates to default").addOptions(
                    OptionData(
                        OptionType.STRING,
                        "template",
                        "The template to delete or '*' to reset all templates",
                        false
                    ).setAutoComplete(true), // Enable autocomplete for templates
                )
            ).setDefaultPermissions(DefaultMemberPermissions.DISABLED)
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        val code = event.getOption("code")?.asString
        val template = event.getOption("template")?.asString
        val type = if (code != null) "code" else "template"
        when (type) {
            code -> RedeemXAPI.code.deleteCode(code)
            template -> RedeemXAPI.template.deleteTemplate(template)
        }
        val message = ""
        event.reply("```\n$message\n```").queue()
    }

    override fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
        val focusedOption = event.focusedOption.name
        val query = event.focusedOption.value.lowercase() // User's input for filtering
        val maxChoices = 25 // Discord's limit for choices

        return when (focusedOption) {
            "code" -> {
                val availableCodes = RedeemXAPI.code.getCodes()
                availableCodes.filter { it.lowercase().contains(query) } // Filter based on the query
                    .take(maxChoices) // Limit to 25 results
                    .map { Command.Choice(it, it) } // Map to Command.Choice
            }

            "template" -> {
                val availableTemplates = RedeemXAPI.template.getTemplates()
                availableTemplates.filter { it.lowercase().contains(query) } // Filter based on the query
                    .take(maxChoices) // Limit to 25 results
                    .map { Command.Choice(it, it) } // Map to Command.Choice
            }

            else -> emptyList()
        }
    }

}