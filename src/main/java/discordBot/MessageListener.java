package discordBot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter
{
    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        if(event.getAuthor().isBot())return;
        if(!event.getMessage().getContentRaw().equals("!Mot"))return;
        System.out.println(event.getMessage().getContentRaw());
        System.out.println(event.getGuild().getId());
        event.getChannel().sendMessage(event.getMessage()).queue();
    }
}