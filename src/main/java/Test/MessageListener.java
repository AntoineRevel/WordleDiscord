package Test;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;

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