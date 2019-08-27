package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

class music:ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): String {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override val options: List<Option>
		get() = TODO(
				"not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val permission: Int
		get() = TODO(
				"not implemented") //To change initializer of created properties use File | Settings | File Templates.
}