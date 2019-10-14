package com.wireless4024.discordbot.command.database

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.PrettyPrinter
import com.wireless4024.discordbot.internal.SkipArguments
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import java.sql.DriverManager
import java.sql.ResultSet

@SkipArguments
class sqlite : ICommandBase {

	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		val msg = event.msg
		if (msg.equals("destroy", true) || msg.equals("clean", true)) {
			event.configuration.sqliteInstance?.close()
			event.configuration.sqliteInstance = null
			return "destroy database successful"
		}
		if (msg.equals("init", true)) {
			event.configuration.sqliteInstance = DriverManager.getConnection("jdbc:sqlite::memory:")
			return "initialized database successful"
		}
		val db = event.configuration.sqliteInstance
		if (db == null || db.isClosed) {
			event.configuration.sqliteInstance = DriverManager.getConnection("jdbc:sqlite::memory:")
			throw RuntimeException("database instance not initialized")
		}
		return if (msg.startsWith("SELECT", true))
			"```\n${toString(db.createStatement().also { it.closeOnCompletion() }.executeQuery(msg))}\n```"
		else
			"```\n${db.createStatement().also { it.closeOnCompletion() }.executeUpdate(msg)} row(s) affected\n```"
	}

	companion object {
		@JvmStatic fun toString(Rset: ResultSet, lineLength: Int = 42): String {
			val columns = 1..Rset.metaData.columnCount
			val result = mutableListOf<Array<String>>()
			val colNames = Array(columns.last) { "" }
			for (i in columns)
				colNames[i - 1] = Rset.metaData.getColumnName(i)
			result.add(colNames)
			while (Rset.next()) {
				val row = Array(columns.last) { "" }
				for (i in columns)
					row[i - 1] = Rset.getString(i)
				result.add(row)
			}
			if (columns.last == 1)
				return result.joinToString("\n") { it.first() }
			return PrettyPrinter.format(result.toTypedArray(), lineLength)
		}
	}

	override val options: List<Option> = listOf()
	override val permission: Int = 0
}