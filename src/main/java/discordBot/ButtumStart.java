package discordBot;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public class ButtumStart extends ListenerAdapter {
    private final Bot bot;

    public ButtumStart(Bot bot) {
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
                ).queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event){
        String id=event.getComponentId();
        if (id.equals("menu:play")){
            event.getChannel().sendMessage("jouer").queue();
            event.reply("ok").queue();
            //event.getButton().asDisabled();

        } else if(id.equals("menu:language")){
            event.editMessage("langue ok").queue();
        }

    }

}
