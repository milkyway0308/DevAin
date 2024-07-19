package skywolf46.devain.controller.commands.discord.openai

import arrow.core.toOption
import net.dv8tion.jda.api.entities.MessageEmbed
import skywolf46.devain.discord.command.EnhancedDiscordCommand
import skywolf46.devain.discord.util.EmbedConstructor
import skywolf46.devain.discord.util.mapFalse
import skywolf46.devain.model.api.openai.completion.OpenAIGPTMessage
import skywolf46.devain.model.api.openai.completion.OpenAIGPTRequest
import skywolf46.devain.model.api.openai.completion.OpenAIGPTResponse
import skywolf46.devain.util.TimeUtil

abstract class GPTCommand(command: String, description: String) :
    EnhancedDiscordCommand(command, description, command.toOption()) {
    fun isEmbedCompatible(request: OpenAIGPTRequest, response: OpenAIGPTResponse): Boolean {
        val requestMessage = request.messages.find { it.role == OpenAIGPTMessage.Role.USER }!!
        val responseMessage = response.answers.last()
        return (request.hidePrompt || requestMessage.content.find { it.first == "text" }?.second.toString().length < 4096) && (responseMessage.message.content.find { it.first == "text" }?.second.toString().length < 1024 - 6)
    }

    fun buildEmbedded(request: OpenAIGPTRequest, response: OpenAIGPTResponse): MessageEmbed {
        val requestMessage = request.messages.find { it.role == OpenAIGPTMessage.Role.USER }!!
        val responseMessage = response.answers.last()
        return EmbedConstructor().withColor(162, 103, 181).withTitle("Request complete - ${request.modelName}")
            .withDescription("_프롬프트 숨겨짐_".mapFalse(request.hidePrompt) {
                requestMessage.content.find { it.first == "text" }?.second.toString()
            }).withField("Response", responseMessage.message.content.find { it.first == "text" }?.second.toString())
            .withInlineField(
                "Tokens", box(
                    "%,d+%,d=%,d".format(
                        response.usage.promptToken, response.usage.completionToken, response.usage.totalToken
                    )
                )
            ).withInlineField("Process", box(TimeUtil.toTimeString(System.currentTimeMillis() - request.createdOn)))
            .withNullableInlineField(request.maxTokens, "Max Token") { box(it.toString()) }
            .withNullableInlineField(request.top_p, "top_p") { box(it.toString()) }
            .withNullableInlineField(request.temperature, "Temperature") { box(it.toString()) }
            .withNullableInlineField(request.frequencyPenalty, "Frequency Penalty") { box(it.toString()) }
            .withNullableInlineField(request.presencePenalty, "Presence Penalty") { box(it.toString()) }
            .withPredicateField(request.showFunctionTrace.toOption(), "Function Trace") {
                response.stackTrace.buildStackTrace()
            }.construct()
    }
}