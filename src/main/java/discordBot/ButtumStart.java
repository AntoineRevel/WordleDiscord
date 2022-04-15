package discordBot;

import WordleApp.ApplicationMots;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ButtumStart extends ListenerAdapter {
    private final Bot bot;
    public static final String code = "Mot";
    private static final List<Button> buttonsMenu = new ArrayList<>() {{
        add(Button.of(ButtonStyle.SUCCESS, "menu:play", "Play", Emoji.fromUnicode("\uD83D\uDD75️")));
        add(Button.of(ButtonStyle.PRIMARY, "menu:language", "Language", Emoji.fromUnicode("\uD83D\uDE03")));
        add(Button.of(ButtonStyle.PRIMARY, "menu:lettres", "Number of letters", Emoji.fromUnicode("\uD83D\uDD24")));
    }};

    private static final List<Button> buttonsMenuOff = getbuttonsMenuOff();


    private String language = "english";
    private int size = 5;
    private boolean firstLetter=false;

    private boolean partieEnCour = false;

    private String getPhraseMenu() {
        return firstLetter ?  "Let's play with " + size + " letter words in " + language + ", with the first letter!" : "Let's play with " + size + " letter words in " + language + "!";
    }

    public ButtumStart(Bot bot) {
        this.bot = bot;
    }

    private static List<Button> getbuttonsMenuOff() {
        return buttonsMenu.stream().map(Button::asDisabled).toList();
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();
        MessageChannel tc = event.getChannel();
        User author = event.getAuthor();

        if (author.isBot()) return;
        if (msg.equalsIgnoreCase(code) && partieEnCour) {
            event.getChannel().sendMessage("A session is already in progress.").queue();
        } else if (msg.equalsIgnoreCase(code)) {
            partieEnCour = true;
            tc.sendMessage("Hello " + author.getName() + ". " + getPhraseMenu())
                    .setActionRow(buttonsMenu).queue();
        }

    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        switch (id) {
            case "menu:play" ->
                    //event.getChannel().sendMessage("jouer").queue();
                    event.editMessage(getPhraseMenu()).setActionRow(buttonsMenuOff).queue(this::startParty);

            case "menu:language" -> event.editMessage("Choice of the language of the words to use.").setActionRow(
                    Button.of(ButtonStyle.SECONDARY, "language:en", "English", Emoji.fromUnicode("\uD83C\uDDFA\uD83C\uDDF8")),//
                    Button.of(ButtonStyle.SECONDARY, "language:fr", "French", Emoji.fromUnicode("\uD83C\uDDEB\uD83C\uDDF7")) //🇫🇷
            ).queue();
            case "menu:lettres" -> event.editMessage("Type the number of letters in the word to guess in " + event.getChannel().getName() + ".")
                    .setActionRow(buttonsMenuOff).queue(this::typeSize);
            case "language:en" -> {
                language = "english";
                menu(event);
            }
            case "language:fr" -> {
                language = "french";
                event.editMessage("Start with the first letter ?").setActionRow(
                        Button.of(ButtonStyle.SECONDARY, "language:fr:oui", "Yes", Emoji.fromUnicode("\uD83D\uDC4D")),
                        Button.of(ButtonStyle.SECONDARY, "language:fr:non", "No", Emoji.fromUnicode("\uD83D\uDC4E"))
                ).queue();
            }
            case "language:fr:oui" -> {
                firstLetter=true;
                menu(event);

            }
            case "language:fr:non" -> {
                firstLetter=false;
                menu(event);
            }
        }

    }

    private void startParty(InteractionHook interactionHook) {
        ApplicationMots am = new ApplicationMots(this, interactionHook);
        am.startBest();
    }

    public Bot getBot() {
        return bot;
    }

    public String getLanguage() {
        return language;
    }

    public int getSize() {
        return size;
    }

    public boolean isFirstLetter() {
        return firstLetter;
    }


    private void typeSize(InteractionHook interactionHook) {
        bot.getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                e -> {
                    if (e.getAuthor().isBot() || !e.getChannel().getId().equals(Objects.requireNonNull(interactionHook.getInteraction().getChannel()).getId()))
                        return false;
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
                    menu(e);
                }
                , 1, TimeUnit.MINUTES,
                () -> interactionHook.getInteraction().getMessageChannel().sendMessage("You didn't respond in time!").queue()

        );
    }



    private void menu(Event event) {
        if (event.getClass().equals(ButtonInteractionEvent.class)){
            ButtonInteractionEvent bte= (ButtonInteractionEvent) event;
            bte.editMessage("Ok " + bte.getUser().getName() + "! " + getPhraseMenu()).setActionRow(
                    buttonsMenu
            ).queue();
        }
        if (event.getClass().equals(MessageReceivedEvent.class)){
            MessageReceivedEvent mre= (MessageReceivedEvent) event;
            mre.getChannel().sendMessage("Ok " + mre.getAuthor().getName() + "! "+getPhraseMenu())
                    .setActionRow(buttonsMenu).queue();
        }

    }


    public void setPartieEnCour(boolean partieEnCour) {
        this.partieEnCour = partieEnCour;
    }

}
