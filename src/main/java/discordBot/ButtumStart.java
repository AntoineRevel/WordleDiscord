package discordBot;

import WordleApp.ApplicationMots;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.concurrent.TimeUnit;

public class ButtumStart extends ListenerAdapter {
    private final Bot bot;

    private String language = "english";
    private int size = 5;
    private boolean partieEnCour=false;

    public String getPhraseMenu() {
        return "Let's play with " + size + " letter words in " + language + "!";
    }

    public ButtumStart(Bot bot) {
        this.bot = bot;
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();
        MessageChannel tc = event.getChannel();
        User author = event.getAuthor();

        if (author.isBot()) return;
        if (msg.equalsIgnoreCase("Mot") && partieEnCour){
            event.getChannel().sendMessage("A session is already in progress.").queue();
        } else if (msg.equalsIgnoreCase("Mot")){
            partieEnCour=true;
            tc.sendMessage("Hello " + author.getName() + ". " + getPhraseMenu())
                    .setActionRow( // Add our Buttons (max 5 per ActionRow)
                            Button.of(ButtonStyle.PRIMARY, "menu:play", "Play", Emoji.fromUnicode("\uD83D\uDD75️")),     // 🕵️‍
                            Button.of(ButtonStyle.PRIMARY, "menu:language", "Language", Emoji.fromUnicode("\uD83D\uDE03")),     //
                            Button.of(ButtonStyle.PRIMARY, "menu:lettres", "Number of letters", Emoji.fromUnicode("\uD83D\uDD24")) // 🔤
                    ).queue();
        }

    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        switch (id) {
            case "menu:play" ->
                    //event.getChannel().sendMessage("jouer").queue();
                    event.editMessage(getPhraseMenu()).setActionRow(
                            Button.of(ButtonStyle.PRIMARY, "menu:play", "Play", Emoji.fromUnicode("\uD83D\uDD75️")).asDisabled(),     // 🕵️‍
                            Button.of(ButtonStyle.PRIMARY, "menu:language", "Language", Emoji.fromUnicode("\uD83D\uDE03")).asDisabled(),     //
                            Button.of(ButtonStyle.PRIMARY, "menu:lettres", "Number of letters", Emoji.fromUnicode("\uD83D\uDD24")).asDisabled() // 🔤
                    ).queue(this::startParty);
            case "menu:language" -> event.editMessage("Choice of the language of the words to use.").setActionRow(
                    Button.of(ButtonStyle.SECONDARY, "language:en", "English", Emoji.fromUnicode("\uD83C\uDDFA\uD83C\uDDF8")),//
                    Button.of(ButtonStyle.SECONDARY, "language:fr", "French", Emoji.fromUnicode("\uD83C\uDDEB\uD83C\uDDF7")) //🇫🇷
            ).queue();
            case "menu:lettres" -> event.editMessage("Type the number of letters in the word to guess in " + event.getChannel().getName() + ".").setActionRow(
                    Button.of(ButtonStyle.PRIMARY, "menu:play", "Play", Emoji.fromUnicode("\uD83D\uDD75️")).asDisabled(),     // 🕵️‍
                    Button.of(ButtonStyle.PRIMARY, "menu:language", "Language", Emoji.fromUnicode("\uD83D\uDE03")).asDisabled(),     //
                    Button.of(ButtonStyle.PRIMARY, "menu:lettres", "Number of letters", Emoji.fromUnicode("\uD83D\uDD24")).asDisabled()
            ).queue(this::entreLettre);
            case "language:en" -> {
                language = "english";
                menu(event);
            }
            case "language:fr" -> {
                language = "french";
                menu(event);
            }
        }

    }

    private void startParty(InteractionHook interactionHook){
        ApplicationMots am=new ApplicationMots(language,size,bot,interactionHook);
        am.start();
    }

    private void entreLettre(InteractionHook interactionHook) {
        bot.getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                e -> {
                    if (e.getAuthor().isBot()) return false;
                    try {
                        int i = Integer.parseInt(e.getMessage().getContentRaw());
                        if (i > 15) {
                            e.getChannel().sendMessage("The value must be between 2 and 15. Type again!").queue();
                        } else {
                            return true;
                        }
                    } catch (NumberFormatException exception) {
                        e.getChannel().sendMessage("Expecting a value for the number of letters in the words. Type again!").queue();
                        return false;
                    }
                    return false;
                },
                e -> {
                    size = Integer.parseInt(e.getMessage().getContentRaw());
                    e.getChannel().sendMessage("Ok " + e.getAuthor().getName() + "! " + getPhraseMenu()).setActionRow(
                            Button.of(ButtonStyle.PRIMARY, "menu:play", "Play", Emoji.fromUnicode("\uD83D\uDD75️")),     // 🕵️‍
                            Button.of(ButtonStyle.PRIMARY, "menu:language", "Language", Emoji.fromUnicode("\uD83D\uDE03")),     //
                            Button.of(ButtonStyle.PRIMARY, "menu:lettres", "Number of letters", Emoji.fromUnicode("\uD83D\uDD24")) // 🔤
                    ).queue();
                }
                , 1, TimeUnit.MINUTES,
                () -> interactionHook.getInteraction().getMessageChannel().sendMessage("You didn't respond in time!").queue()

        );
    }

    private void menu(ButtonInteractionEvent event) {
        event.editMessage("Ok " + event.getUser().getName() + "! " + getPhraseMenu()).setActionRow(
                Button.of(ButtonStyle.PRIMARY, "menu:play", "Play", Emoji.fromUnicode("\uD83D\uDD75️")),     // 🕵️‍
                Button.of(ButtonStyle.PRIMARY, "menu:language", "Language", Emoji.fromUnicode("\uD83D\uDE03")),     //
                Button.of(ButtonStyle.PRIMARY, "menu:lettres", "Number of letters", Emoji.fromUnicode("\uD83D\uDD24")) // 🔤
        ).queue();
    }


}
