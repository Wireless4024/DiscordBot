import com.wireless4024.discordbot.internal.ConfigurationCache;
import com.wireless4024.discordbot.internal.Handler;
import com.wireless4024.discordbot.internal.Property;
import com.wireless4024.discordbot.internal.Utils;
import net.dv8tion.jda.api.JDABuilder;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args.length > 0 && args[0].equals("silent")) {
			System.out.close(); System.err.close();
		}

		System.setProperty("idea.io.use.fallback", "true"); try {
			Property.Companion.setJDA(JDABuilder.createDefault(Property.getTOKEN())//.setToken()
			                                    //.setAudioSendFactory(NativeAudioSendFactory())
			                                    .addEventListeners(Handler.instance).build());
		} catch (Throwable e) {
			Utils.error(e.getMessage() == null ? e.toString() : e.getMessage());
		}

		System.out.println("mongo error may show but not affected the bot"); ConfigurationCache.Companion.init();
	}
}
