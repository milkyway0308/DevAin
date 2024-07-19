package skywolf46.devain.platform.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import skywolf46.devain.discord.DiscordWrapper
import skywolf46.devain.discord.command.BasicDiscordCommand
import skywolf46.devain.discord.command.EnhancedDiscordCommand
import skywolf46.devain.discord.data.lifecycle.DiscordFallback
import kotlin.system.exitProcess

class DiscordBot {
    lateinit var jda: JDA


    private val commands = mutableListOf<BasicDiscordCommand>()


    fun registerCommands(vararg command: BasicDiscordCommand): DiscordBot {
        commands.addAll(command)
        return this
    }

    fun registerCommands(vararg command: EnhancedDiscordCommand): DiscordBot {
        commands.addAll(command)
        return this
    }

    internal fun finishSetup(apiToken: String) {
        kotlin.runCatching {
            jda = JDABuilder.create(apiToken, GatewayIntent.values().toList()).addEventListeners()
                .setStatus(OnlineStatus.IDLE).setActivity(Activity.listening("안")).build().awaitReady()
        }.onFailure {
            println("초기화 실패; 봇 초기화 중 오류가 발생하였습니다.")
            it.printStackTrace()
            exitProcess(-1)
        }

        DiscordWrapper(jda, DiscordFallback())
            .registerCommands(*commands.toTypedArray())
            .finishSetup()
    }
}