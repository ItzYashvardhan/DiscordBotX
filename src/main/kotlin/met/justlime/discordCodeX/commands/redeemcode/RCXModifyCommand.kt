package met.justlime.discordCodeX.commands.redeemcode

import me.justlime.redeemX.api.RedeemXAPI
import me.justlime.redeemX.api.RedeemXAPI.modifyCode
import me.justlime.redeemX.api.RedeemXAPI.modifyTemplate
import me.justlime.redeemX.api.RedeemXAPI.placeHolder
import me.justlime.redeemX.enums.JTab
import met.justlime.discordCodeX.commands.JRedeemCode
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

class RCXModifyCommand : JRedeemCode {
    override fun buildCommand(): CommandData {
        return Commands.slash("modify", "Modify a redeem code or template").addSubcommands(
            // Subcommand for modifying a code
            SubcommandData("code", "Modify a redeem code").addOptions(
                OptionData(OptionType.STRING, "code", "The code to modify", true).setAutoComplete(true), // Enable autocomplete for codes
                OptionData(OptionType.STRING, "property", "The property to modify", true).setAutoComplete(true),
                OptionData(OptionType.STRING, "value", "The new value for the property", false),
            ),
            // Subcommand for modifying a template
            SubcommandData("template", "Modify a template").addOptions(
                OptionData(
                    OptionType.STRING, "template", "The template to modify", true
                ).setAutoComplete(true), // Enable autocomplete for templates
                OptionData(OptionType.STRING, "property", "The property to modify", true).setAutoComplete(true),
                OptionData(OptionType.STRING, "value", "The new value for the property", false),
            )
        )
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        val code = event.getOption("code")?.asString
        val template = event.getOption("template")?.asString
        val type = if (code != null) "code" else "template"
        val property = event.getOption("property")?.asString ?: return
        val value = event.getOption("value")?.asString


        when (type) {
            "code" -> if (value != null) modifyCode(code?: return, property, value) else modifyCode(code?: return, property)
            "template" -> if (value != null) modifyTemplate(template ?: return, property, value) else modifyTemplate(template ?: return, property)
        }

        val message = placeHolder.sentMessage.replace(",", "\n")
        event.reply("```\n$message\n```").queue()

    }

    override fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
        val focusedOption = event.focusedOption.name
        val property = mutableListOf(
            JTab.Modify.ENABLED,
            JTab.Modify.SYNC,
            JTab.Modify.SET_REDEMPTION,
            JTab.Modify.SET_PLAYER_LIMIT,
            JTab.Modify.SET_COMMAND,
            JTab.Modify.ADD_COMMAND,
            JTab.Modify.REMOVE_COMMAND,
            JTab.Modify.SET_DURATION,
            JTab.Modify.ADD_DURATION,
            JTab.Modify.REMOVE_DURATION,
            JTab.Modify.SET_PERMISSION,
            JTab.Modify.SET_PIN,
            JTab.Modify.SET_TARGET,
            JTab.Modify.ADD_TARGET,
            JTab.Modify.REMOVE_TARGET,
            JTab.Modify.SET_COOLDOWN,
            JTab.Modify.SET_TEMPLATE,
        )
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

            "property" -> property.filter { it.lowercase().contains(query) }.take(maxChoices).map { Command.Choice(it, it) }
            "value" -> emptyList()
            else -> emptyList()
        }
    }
}