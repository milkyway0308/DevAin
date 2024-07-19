package skywolf46.devain.controller.commands.discord.etc

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.utils.FileUpload
import org.scilab.forge.jlatexmath.TeXConstants
import org.scilab.forge.jlatexmath.TeXFormula
import skywolf46.devain.discord.command.EnhancedDiscordCommand
import java.awt.Color
import java.awt.Insets
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import javax.swing.JLabel


class LatexCompileCommand : EnhancedDiscordCommand("math-compile", "주어진 LaTex 코드를 이미지로 컴파일합니다.") {
    override fun modifyCommandData(options: SlashCommandData) {
        options.addOptions(
            OptionData(OptionType.STRING, "latex", "LaTex 구문", true),
            OptionData(OptionType.INTEGER, "rgba_background", "이미지의 배경 색입니다. 지정하지 않는다면, 기본 값은 하얀색입니다."),
            OptionData(OptionType.INTEGER, "rgba_font", "이미지의 글자 색입니다. 지정하지 않는다면, 기본 값은 검정색입니다."),
        )
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        event.defer { _, hook ->
            runCatching {
                val latex = event.getOption("latex")!!.asString
                val formula = TeXFormula(latex)
                // Note: Old interface for creating icons:
                //TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 30);
                // Note: New interface using builder pattern (inner class):
                val icon = formula.TeXIconBuilder().setStyle(TeXConstants.STYLE_DISPLAY).setSize(30f).build()
                icon.insets = Insets(5, 5, 5, 5)
                val image = BufferedImage(icon.iconWidth, icon.iconHeight, BufferedImage.TYPE_INT_ARGB)
                val g2 = image.createGraphics()
                g2.color =
                    event.getOption("rgba_background")?.asLong?.toInt()?.let { Color(it, true) } ?: Color(
                        255,
                        255,
                        255,
                        255
                    )
                g2.fillRect(0, 0, icon.iconWidth, icon.iconHeight)
                val jl = JLabel()
                jl.foreground = event.getOption("rgba_font")?.asLong?.toInt()?.let { Color(it, true) } ?: Color.BLACK
                icon.paintIcon(jl, g2, 0, 0)
                val baos = ByteArrayOutputStream()
                ImageIO.write(image, "png", baos)
                val bytes = baos.toByteArray()
                hook.sendFiles(FileUpload.fromData(bytes, "latex.png")).queue()
            }.onFailure {
                hook.sendMessage("LaTex 컴파일 중 오류가 발생했습니다: ${it.message}").queue()
            }
        }
    }
}