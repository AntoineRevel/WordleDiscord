package discordBot;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class Bot {

    private final EventWaiter eventWaiter = new EventWaiter();

    public static void main(String[] args) {
        try {
            new Bot().start();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() throws LoginException, InterruptedException {
        JDABuilder.createDefault("privateToken")
                .addEventListeners(
                        new ButtumStart(this),
                        eventWaiter
                )
                .build()
                .awaitReady();
    }

    public EventWaiter getEventWaiter() {
        return eventWaiter;
    }
}
