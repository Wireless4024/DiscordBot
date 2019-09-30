import com.wireless4024.discordbot.internal.ConfigurationCache;
import com.wireless4024.discordbot.internal.Property;
import com.wireless4024.discordbot.internal.Utils;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args.length > 0 && args[0].equals("silent")) {
			System.out.close(); System.err.close();
		}

		System.setProperty("idea.io.use.fallback", "true"); try {
			Property.Companion.setJDA(new JDABuilder(AccountType.BOT).setToken(Property.getTOKEN())
			                                                         //.setAudioSendFactory(NativeAudioSendFactory())
			                                                         .addEventListeners(Utils.getGlobalEvent())
			                                                         .build());
		} catch (Throwable e) {
			Utils.error(e.getMessage() == null ? e.toString() : e.getMessage());
		}

		ConfigurationCache.Companion.init();
	}
}
