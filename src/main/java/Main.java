import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.wireless4024.discordbot.internal.Property;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;

public class Main {
	public static void main(String[] args) throws Exception {
		System.setProperty("idea.io.use.fallback", "true");

		new JDABuilder(AccountType.BOT).setToken(Property.getTOKEN())//.setAudioSendFactory(new NativeAudioSendFactory())
		                               .addEventListeners(new com.wireless4024.discordbot.internal.Handler()).build();
	}
}
