package discordBot;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
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

        tc.sendMessage("Hello " + author.getName() + ". Let's solve together!")
                .setActionRow( // Add our Buttons (max 5 per ActionRow)
                        Button.of(ButtonStyle.PRIMARY, "menu:play", "Play", Emoji.fromUnicode("\uD83D\uDD75️")),     // 🕵️‍
                        Button.of(ButtonStyle.PRIMARY, "menu:language", "Language", Emoji.fromUnicode("\uD83D\uDE03")),     //
                        Button.of(ButtonStyle.PRIMARY, "menu:lettres", "Number of letters", Emoji.fromUnicode("\uD83D\uDD24")), // 🔤
                        Button.of(ButtonStyle.PRIMARY, "menu:Cancel", "Cancel", Emoji.fromUnicode("\uD83D\uDED1"))      // 🛑
                ).queue(
                        this::accept);
    }

    private void accept(Message message) {
        bot.getEventWaiter().waitForEvent(
                ButtonInteractionEvent.class,
                e -> {
                    if (e.getMessageIdLong() != message.getIdLong())return false;
                    if (e.getUser().getIdLong() != message.getAuthor().getIdLong())return false;
                    return !e.isAcknowledged();
                },
                e -> e.getHook().sendMessage("You selected " + e.getComponentId() + "!").queue()
                , 1, TimeUnit.MINUTES,
                () -> message.getChannel().sendMessage("You didn't respond in time!").queue()

        );
    }
}

/*
private void accept(Message message) {
        bot.getEventWaiter().waitForEvent(
                ButtonInteractionEvent.class,
                e -> {
                    return e.getMessage().getContentRaw().equals("ok");
                },
                messageReceivedEvent -> System.out.println(messageReceivedEvent.getMessage().getContentRaw())
                , 1, TimeUnit.MINUTES,
                () -> {
                    message.getChannel().sendMessage("You didn't respond in time!").queue();
                }

        );
    }
 */

