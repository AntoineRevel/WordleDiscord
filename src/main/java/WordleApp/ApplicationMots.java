package WordleApp;

import discordBot.Bot;
import discordBot.ButtumStart;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static discordBot.ButtumStart.code;

public class ApplicationMots {
    public final String GRAS = "**";

    public final String cheminFR = "src/main/java/WordleApp/ressources/motsFR.txt";
    public final String cheminAn = "src/main/java/WordleApp/ressources/mots.txt";
    private final String[] options = {"1- Jouer ",
            "2- Langue",
            "3- Longueur mots",
            "4- Exit",
    };
    private final String langue;
    private final int longeur;

    private final Bot bot;
    private final MessageChannel messageChannel;
    private final MotsPossible MP;
    private String lastProposition;
    private final ButtumStart bs;


    public ApplicationMots(ButtumStart bs, InteractionHook ih) {
        String langue=bs.getLanguage();
        if (langue.equals("french")) {
            this.langue = cheminFR;
        } else if (langue.equals("english")) {
            this.langue = cheminAn;
        } else {
            throw new RuntimeException("Langue inconnu " + langue);
        }
        this.longeur = bs.getSize();
        this.bot = bs.getBot();
        this.messageChannel = ih.getInteraction().getMessageChannel();
        this.bs=bs;

        MP = new MotsPossible(longeur, this.langue, messageChannel);

    }


    private void afficheRegle() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("How to code responses?");
        embedBuilder.addField("0  :  For a letter that is not in the word.  (grey)", "", false);
        embedBuilder.addField("1  :  For a letter in the word but not in the right place.  (yellow)", "", false);
        embedBuilder.addField("2  :  For a letter in the right place.  (green)", "", false);
        messageChannel.sendMessageEmbeds(embedBuilder.build()).queue();

    }


    public void start() {
        afficheRegle();
        lastProposition = ouverture(MP);
        choixReponse();
    }

    public void start2(String rep) {
        MP.elimination(new Reponse(lastProposition, rep));
        if (MP.getMotsPossible().size()>1){
            List<String> choix = MP.choix();
            int size = choix.size();

            if (size == 1) {
                lastProposition = choix.get(0);
            } else {
                choixprop(choix);
                return;
                /*int indice = 1choixEgaliter(size);

                lastProposition = choix.get(indice - 1);

                System.out.println("Proposition : " + ANSI_RED + lastProposition + ANSI_RESET);*/
            }
            choixReponse();
        } else {
            finPartie();
            System.out.print(" Success !");
        }
    }

    private void finPartie(){
        messageChannel.sendMessage("The game is over you can retype "+"*"+code+"*"+" to play again.").queue();
        bs.setPartieEnCour(false);
        System.out.println(java.time.LocalDate.now()+" "+java.time.LocalTime.now()+ " : "+messageChannel.getName());
    }

    private void choixprop(List<String> choix){
        Collection<Button> listButtum=new ArrayList<>();
        Collection<Button> listButtumOff=new ArrayList<>();
        for (String prop:choix){
            listButtum.add(Button.of(ButtonStyle.PRIMARY,"choix:"+prop,prop));
            listButtumOff.add(Button.of(ButtonStyle.PRIMARY,"choix:"+prop,prop).asDisabled());
        }
        messageChannel.sendMessage("Choose one.").setActionRow(listButtum).queue();
        bot.getEventWaiter().waitForEvent(
                ButtonInteractionEvent.class,
                buttonInteractionEvent -> buttonInteractionEvent.getComponentId().contains("choix:"),
                e->{
                    e.editMessage(e.getMessage()).setActionRow(listButtumOff).queue();
                    lastProposition=e.getInteraction().getComponentId().substring(6);
                    messageChannel.sendMessage("> "+ MotsPossible.GRAS + lastProposition+ MotsPossible.GRAS).queue();
                    choixReponse();

                },1,TimeUnit.MINUTES,
                () -> {
                    messageChannel.sendMessage("You didn't respond in time!").queue();
                    finPartie();
                }

        );
    }



    private void choixReponse() {
        messageChannel.sendMessage("Game response :").queue(this::recupLastSaisie);
    }


    private void recupLastSaisie(Message message) {
        bot.getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                e -> {
                    if (e.getAuthor().isBot() || !e.getChannel().getId().equals(messageChannel.getId())) return false;
                    String msg = e.getMessage().getContentRaw();
                    if (Reponse.verifRep(msg) && msg.length() == longeur) {
                        return true;
                    } else {
                        messageChannel.sendMessage("Type only " + longeur + " values between 0 and 2 (see meaning at the beginning)").queue();
                    }
                    return false;
                },
                e -> start2(e.getMessage().getContentRaw())
                , 1, TimeUnit.MINUTES,
                () -> {
                    message.getChannel().sendMessage("You didn't respond in time!").queue();
                    finPartie();
                }

        );
    }



    private String ouverture(MotsPossible MP) {
        HashMap<Integer, String> bestOuverture = new HashMap<>();
        if (langue.equals(cheminAn)) {
            bestOuverture.put(2, "ho" + GRAS + " with an expected value of " + GRAS + "27.489");
            bestOuverture.put(3, "eat" + GRAS + " with an expected value of " + GRAS + "462.316");
            bestOuverture.put(4, "sale" + GRAS + " with an expected value of " + GRAS + "2146.642");
            bestOuverture.put(5, "tares" + GRAS + " with an expected value of " + GRAS + "4175.682");
        }

        if (langue.equals(cheminFR)) {
            bestOuverture.put(2, "au" + GRAS + " with an expected value of " + GRAS + "48,374");
            bestOuverture.put(3, "aie" + GRAS + " with an expected value of " + GRAS + "374,294");
            bestOuverture.put(4, "taie" + GRAS + " with an expected value of " + GRAS + "1929,883");

        }
        int longeur = MP.getSize();
        String prop;
        if (bestOuverture.containsKey(longeur)) {
            prop = bestOuverture.get(longeur);
            messageChannel.sendMessage("Best opening : ").queue();
            messageChannel.sendMessage("> " + GRAS + prop + GRAS + " eliminated words.").queue();
            return prop.substring(0, longeur);
        }
        prop = MP.random();
        if (longeur == 6) {
            messageChannel.sendMessage("Proposal of a random opening :").queue();
            messageChannel.sendMessage("> " + MotsPossible.GRAS + prop + MotsPossible.GRAS).queue();
            messageChannel.sendMessage("Calculation of the expectation...").queue();
            messageChannel.sendMessage(" with an expected value of " + GRAS + String.format("%.3f", MP.calculEsperance(prop)) + GRAS + " eliminated words.").queue();



        } else {
            messageChannel.sendMessage("Proposal of a random opening :").queue();
            messageChannel.sendMessage("> " + GRAS + prop + GRAS).queue();
        }
        //System.out.println(prop);
        return prop;
    }
}
