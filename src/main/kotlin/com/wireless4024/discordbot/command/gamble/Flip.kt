package com.wireless4024.discordbot.command.gamble

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import java.util.concurrent.ThreadLocalRandom

class Flip : ICommandBase {
    override fun invoke(args: CommandLine, event: MessageEvent): Any {
        val random = ThreadLocalRandom.current().nextInt(0, 100)
        val value = random < (args.argList?.firstOrNull()?.toIntOrNull()?.coerceIn(0, 100) ?: 50)
        return if (value) "HEAD ($random)" else "TAIL ($random)"
    }

    override val options: List<Option> = listOf()
    override val permission: Int = 0
}