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

class RCXGenCommand : JRedeemCode {
    override fun buildCommand(): CommandData {
        return Commands.slash("generate", "Generate redeem codes").addOptions(
            OptionData(OptionType.INTEGER, "digit", "Number of digits for the code", false).setMinValue(1)
                .setMaxValue(25), // Example range for digits
            OptionData(OptionType.STRING, "custom", "Custom string for the code", false),
            OptionData(OptionType.INTEGER, "amount", "Number of codes to generate", false).setMinValue(1)
                .setMaxValue(100), // Example range for amount
            OptionData(
                OptionType.STRING,
                "template",
                "Template to use for the codes",
                false
            ).setAutoComplete(true) // Enable autocomplete for templates
        ).setDefaultPermissions(DefaultMemberPermissions.DISABLED)
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        val digit = event.getOption("digit")?.asInt
        val custom = event.getOption("custom")?.asString
        val amount = event.getOption("amount")?.asInt ?: 1
        val template = event.getOption("template")?.asString ?: "DEFAULT"
        var customCode = if (custom != null) RedeemXAPI.code.generateCode(custom, template) else null
        val digitCode = if (digit != null) RedeemXAPI.code.generateCode(digit, template, amount) else null

        val generatedCodes = mutableListOf<String>()
        val codes = RedeemXAPI.code.getCodes()
        if (customCode != null && codes.contains(customCode.code)) {
            customCode = null
        }
        if (customCode != null) {

            generatedCodes.add(customCode.code)
            RedeemXAPI.code.upsertCode(customCode)

        }
        if (digitCode != null) {

            generatedCodes.addAll(digitCode.map { it.code })
            RedeemXAPI.code.upsertCodes(digitCode.toList())
        }

        if (generatedCodes.isEmpty()) {
            event.reply("Failed to generate any codes. Please check your parameters.").setEphemeral(true).queue()
            return
        }
        val message = generatedCodes.joinToString(" ")

        event.reply("```\n$message - $template```").queue()
    }

    override fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
        val focusedOption = event.focusedOption.name
        return when (focusedOption) {
            "template" -> {
                // Fetch available templates from RedeemXAPI
                val availableTemplates = RedeemXAPI.template.getTemplates()
                availableTemplates.map { Command.Choice(it, it) }
            }

            "digit" -> {
                // Provide a range of digits for the user to select
                (1..10).map { Command.Choice(it.toString(), it.toString()) }
            }

            else -> emptyList() // Return no suggestions if no valid option is focused
        }
    }
}
