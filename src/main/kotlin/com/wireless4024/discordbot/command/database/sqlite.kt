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
			event.configuration.destroySQLiteInstance()
			return "destroy database successful"
		}
		if (msg.equals("init", true)) {
			event.configuration.sqliteInstance = DriverManager.getConnection("jdbc:sqlite::memory:")
			return "initialized database successful"
		}
		if (msg.equals("enter", true)) {
			if (event.configuration.sqliteInstance == null || (event.configuration.sqliteInstance?.isClosed == true))
				event.configuration.sqliteInstance = DriverManager.getConnection("jdbc:sqlite::memory:")
			event.configuration.registerContext("sqlite",
			                                    event.ch.idLong,
			                                    { executeSQL(it) },
			                                    { it.configuration.destroySQLiteInstance() })
			return "now you can type SQL into chat to execute SQL!"
		}
		return executeSQL(event)
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
			return PrettyPrinter.format(result.toTypedArray(), lineLength)
		}

		fun executeSQL(event: MessageEvent): String {
			if (event.msg.isEmpty()) return "missing sql"
			val db = event.configuration.sqliteInstance
			if (db == null || db.isClosed) {
				event.configuration.sqliteInstance = DriverManager.getConnection("jdbc:sqlite::memory:")
				throw RuntimeException("database instance not initialized")
			}
			val msg = event.msg
			return if (msg.startsWith("SELECT", true))
				"```\n${toString(db.createStatement().also { it.closeOnCompletion() }.executeQuery(msg))}\n```"
			else
				"```\n${db.createStatement().also { it.closeOnCompletion() }.executeUpdate(msg)} row(s) affected\n```"
		}
	}

	override val options: List<Option> = listOf()
	override val permission: Int = 0
}