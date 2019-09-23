import com.wireless4024.discordbot.internal.Property;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args.length > 0 && args[0].equals("silent")) {
			System.out.close(); System.err.close();
		}

		System.setProperty("idea.io.use.fallback", "true");

		new JDABuilder(AccountType.BOT).setToken(Property.getTOKEN())//.setAudioSendFactory(new NativeAudioSendFactory())
		                               .addEventListeners(new com.wireless4024.discordbot.internal.Handler()).build();
	}
}
