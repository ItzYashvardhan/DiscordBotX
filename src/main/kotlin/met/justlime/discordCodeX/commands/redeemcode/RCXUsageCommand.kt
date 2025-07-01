package met.justlime.discordCodeX.commands.redeemcode

import me.justlime.redeemcodex.api.RedeemXAPI
import met.justlime.discordCodeX.commands.JRedeemCode
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

class RCXUsageCommand : JRedeemCode {
    override fun buildCommand(): CommandData {
        return Commands.slash("usage", "View Usages of Code or Template").addSubcommands(
            SubcommandData("code", "View Usages of a Specific Code").addOptions(
                    OptionData(OptionType.STRING, "code", "The code to view usage for", true).setAutoComplete(true), // Enable autocomplete for codes
                ), SubcommandData("template", "View Usages of a Specific Template").addOptions(
                    OptionData(
                        OptionType.STRING,
                        "template",
                        "The template to view usage for",
                        true
                    ).setAutoComplete(true), // Enable autocomplete for templates
                )
        )
            .setGuildOnly(true)
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        val code = event.getOption("code")?.asString
        val template = event.getOption("template")?.asString
        val type = if (code != null) "code" else "template"
         when (type) {
            "code" -> RedeemXAPI.usageCode(code?: return)
            "template" -> RedeemXAPI.usageTemplate(template?: return)
        }
        val message = RedeemXAPI.placeHolder.sentMessage.replace(",", "\n")
        event.reply("```\n$message\n```").queue()

    }

    override fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
        val focusedOption = event.focusedOption.name
        val query = event.focusedOption.value.lowercase() // User's input for filtering
        val maxChoices = 25 // Discord's limit for choices

        return when (focusedOption) {
            "code" -> {
                val availableCodes = RedeemXAPI.getCodes()
                availableCodes.filter { it.lowercase().contains(query) } // Filter based on the query
                    .take(maxChoices) // Limit to 25 results
                    .map { Command.Choice(it, it) } // Map to Command.Choice
            }

            "template" -> {
                val availableTemplates = RedeemXAPI.getTemplates()
                availableTemplates.filter { it.lowercase().contains(query) } // Filter based on the query
                    .take(maxChoices) // Limit to 25 results
                    .map { Command.Choice(it, it) } // Map to Command.Choice
            }

            else -> emptyList()
        }

    }
}