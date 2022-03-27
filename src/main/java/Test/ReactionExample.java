package Test;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.concurrent.TimeUnit;

public class ReactionExample extends ListenerAdapter {

    private final Bot bot;

    public ReactionExample(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
            String msg = event.getMessage().getContentRaw();
            MessageChannel tc = event.getChannel();
            User author = event.getAuthor();

            if (author.isBot()) return;
            if (!msg.equalsIgnoreCase("Mot")) return;

            tc.sendMessage("Hello " + author.getName() + ", do you like apples?")
                    .setActionRow( // Add our Buttons (max 5 per ActionRow)
                            Button.of(ButtonStyle.PRIMARY, "example-bot:button:pet:cat", "Cat", Emoji.fromUnicode("\uD83D\uDC31")),     // 🐱
                            Button.of(ButtonStyle.PRIMARY, "example-bot:button:pet:dog", "Dog", Emoji.fromUnicode("\uD83D\uDC36")),     // 🐶
                            Button.of(ButtonStyle.PRIMARY, "example-bot:button:pet:bunny", "Bunny", Emoji.fromUnicode("\uD83D\uDC30")), // 🐰
                            Button.of(ButtonStyle.PRIMARY, "example-bot:button:pet:fox", "Fox", Emoji.fromUnicode("\uD83E\uDD8A"))      // 🦊
                    ).queue(
                    this::accept);
    }

    private void accept(Message message) {
        bot.getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                e -> {
                    return e.getMessage().getContentRaw().equals("ok");
                },
                messageReceivedEvent -> System.out.println(messageReceivedEvent.getMessage().getContentRaw())
                ,1, TimeUnit.MINUTES,
                () -> {
                   message.getChannel().sendMessage("You didn't respond in time!").queue();
                }

        );
    }
}

