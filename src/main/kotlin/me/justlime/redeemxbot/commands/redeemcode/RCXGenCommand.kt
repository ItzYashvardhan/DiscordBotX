package me.justlime.redeemxbot.commands.redeemcode

import api.justlime.redeemcodex.RedeemXAPI
import me.justlime.redeemxbot.commands.JRedeemCode
import me.justlime.redeemxbot.enums.JMessages
import me.justlime.redeemxbot.utils.JServices
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class RCXGenCommand : JRedeemCode {

    companion object {
        private const val MAX_DIGITS: Long = 25
        private const val MAX_AMOUNT: Long = 100
        private const val DEFAULT_TEMPLATE = "DEFAULT"
    }

    override fun buildCommand(): CommandData {
        return Commands.slash(
            JServices.getMessage(JMessages.GENERATE_COMMAND.path),
            JServices.getMessage(JMessages.GENERATE_DESCRIPTION.path)
        ).addOptions(
            OptionData(
                OptionType.INTEGER,
                JServices.getMessage(JMessages.GENERATE_DIGIT_COMPLETION.path),
                JServices.getMessage(JMessages.GENERATE_DIGIT_DESCRIPTION.path),
                false
            ).setMinValue(1).setMaxValue(MAX_DIGITS),
            OptionData(
                OptionType.STRING,
                JServices.getMessage(JMessages.GENERATE_CUSTOM_COMPLETION.path),
                JServices.getMessage(JMessages.GENERATE_CUSTOM_DESCRIPTION.path),
                false
            ),
            OptionData(
                OptionType.INTEGER,
                JServices.getMessage(JMessages.GENERATE_AMOUNT_COMPLETION.path),
                JServices.getMessage(JMessages.GENERATE_AMOUNT_DESCRIPTION.path),
                false
            ).setMinValue(1).setMaxValue(MAX_AMOUNT),
            OptionData(
                OptionType.STRING,
                JServices.getMessage(JMessages.GENERATE_TEMPLATE_COMPLETION.path),
                JServices.getMessage(JMessages.GENERATE_TEMPLATE_DESCRIPTION.path),
                false
            ).setAutoComplete(true)
        ).setDefaultPermissions(DefaultMemberPermissions.DISABLED)
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        val digit = event.getOption(JServices.getMessage(JMessages.GENERATE_DIGIT_COMPLETION.path))?.asInt
        val custom = event.getOption(JServices.getMessage(JMessages.GENERATE_CUSTOM_COMPLETION.path))?.asString
        val amount = event.getOption(JServices.getMessage(JMessages.GENERATE_AMOUNT_COMPLETION.path))?.asInt ?: 1
        val template = event.getOption(JServices.getMessage(JMessages.GENERATE_TEMPLATE_COMPLETION.path))
            ?.asString ?: DEFAULT_TEMPLATE

        val generatedCodes = mutableListOf<String>()
        val existingCodeSet = RedeemXAPI.code.getCodes().toSet()

        // Handle custom codes
        val customCodeList = custom?.split(" ")
            ?.map(String::trim)
            ?.filter(String::isNotEmpty)
            ?.map { code ->
                if (code.isNotEmpty() && code !in existingCodeSet) RedeemXAPI.code.generateCode(code, template) else null
            } ?: emptyList()

        if (customCodeList.isNotEmpty()) {
            RedeemXAPI.code.upsertCodes(customCodeList.mapNotNull { it })
            generatedCodes.addAll(customCodeList.mapNotNull { it?.code })
        }

        // Handle digit-generated codes
        digit?.let {
            val digitCodes = RedeemXAPI.code.generateCode(digit, template, amount)
            val newDigitCodes = digitCodes.filter { it.code !in existingCodeSet }
            if (newDigitCodes.isNotEmpty()) {
                RedeemXAPI.code.upsertCodes(newDigitCodes)
                generatedCodes.addAll(newDigitCodes.map { it.code })
            }
        }

        // Reply
        if (generatedCodes.isEmpty()) {
            event.reply(JServices.getMessage(JMessages.GENERATE_FAILED.path)).setEphemeral(true).queue()
            return
        }

        val replyMessage = JServices.getMessage(JMessages.GENERATE_SUCCESS.path)
            .replace("{code}", generatedCodes.joinToString(" "))
            .replace("{template}", template)

        event.reply(replyMessage).queue()
    }


    override fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
        val focusedOption = event.focusedOption.name
        return when (focusedOption) {
            JServices.getMessage(JMessages.GENERATE_TEMPLATE_COMPLETION.path) -> {
                RedeemXAPI.template.getTemplates()
                    .take(25)
                    .map { Command.Choice(it, it) }
            }

            JServices.getMessage(JMessages.GENERATE_DIGIT_COMPLETION.path) -> {
                (1..10).map { Command.Choice(it.toString(), it.toString()) }
            }

            else -> emptyList()
        }
    }
}
