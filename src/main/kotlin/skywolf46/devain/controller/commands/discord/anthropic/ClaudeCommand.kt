package skywolf46.devain.controller.commands.discord.anthropic

import arrow.core.toOption
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.utils.FileUpload
import org.koin.core.component.inject
import skywolf46.devain.controller.api.requests.anthropic.ClaudeAPICall
import skywolf46.devain.discord.command.EnhancedDiscordCommand
import skywolf46.devain.model.api.anthropic.ClaudeGenerationRequest
import skywolf46.devain.model.api.anthropic.ClaudeMessage

class ClaudeCommand : EnhancedDiscordCommand("claude") {
    private val apiCall by inject<ClaudeAPICall>()

    override fun modifyCommandData(options: SlashCommandData) {
        options.addOption(OptionType.STRING, "prompt", "요청 프롬프트입니다.", true)
        options.addOption(OptionType.INTEGER, "max-tokens", "최대 토큰입니다.")
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        val request = ClaudeGenerationRequest(
            "claude-3-5-sonnet-20240620",
            listOf(
                ClaudeMessage(
                    ClaudeMessage.ClaudeGenerationRole.USER, event.getOption("prompt")!!.asString

                )
            ),
            4096.toOption()
        )
        event.defer { _, hook ->

            apiCall.call(request).fold({
                hook.sendMessage("오류가 발생했습니다. ${it.getErrorMessage()}").queue()
            }) {
                val message = it.message.joinToString("\n")
                if(message.length >= 2000) {
                    hook.sendFiles(FileUpload.fromData(message.byteInputStream(), "result.txt")).queue()
                } else {
                    hook.sendMessage(message).queue()
                }
            }
        }
    }
}