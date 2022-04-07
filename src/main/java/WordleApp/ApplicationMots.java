package WordleApp;

import discordBot.Bot;
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

import static WordleApp.MotsPossible.GRAS;

public class ApplicationMots {
    public final String ANSI_RESET = "**";
    public final String ANSI_RED = "**";
    public final String ANSI_gras = "**";

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

    private final Scanner saisieUtilisateur; //soon inutile

    public ApplicationMots(String langue, int longeur, Bot bot, InteractionHook ih) {
        if (langue.equals("french")) {
            this.langue = cheminFR;
        } else if (langue.equals("english")) {
            this.langue = cheminAn;
        } else {
            throw new RuntimeException("Langue inconnu " + langue);
        }
        this.longeur = longeur;
        this.bot = bot;
        this.messageChannel = ih.getInteraction().getMessageChannel();
        this.saisieUtilisateur = new Scanner(System.in);

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
            //message fin
            messageChannel.sendMessage("bravo").queue();
        }
    }

    private void choixprop(List<String> choix){
        String propFinal="";
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
                    messageChannel.sendMessage("> "+GRAS+ lastProposition+GRAS).queue();
                    choixReponse();

                },1,TimeUnit.MINUTES,
                () -> messageChannel.sendMessage("You didn't respond in time!").queue()
        );
    }



    private void choixReponse() {
        messageChannel.sendMessage("Game response :").queue(this::recupLastSaisie);
    }


    private void recupLastSaisie(Message message) {
        bot.getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                e -> {
                    if (e.getAuthor().isBot()) return false;
                    String msg = e.getMessage().getContentRaw();
                    if (Reponse.verifRep(msg) && msg.length() == longeur && e.getChannel().getId().equals(messageChannel.getId())) { //On vérifie que le message est envoyé dans le bon salon
                        return true;
                    } else {
                        messageChannel.sendMessage("Type only " + longeur + " values between 0 and 3 (see meaning at the beginning)").queue();
                    }
                    return false;
                },
                e -> start2(e.getMessage().getContentRaw())
                , 1, TimeUnit.MINUTES,
                () -> message.getChannel().sendMessage("You didn't respond in time!").queue()

        );
    }



    private int choixEgaliter(int size) {
        int indice;
        try {
            System.out.print("Choix : ");
            indice = saisieUtilisateur.nextInt();
            if (indice > size) {
                System.out.println("Entrez un entier entre 1 et " + size);
                return choixEgaliter(size);
            }
            return indice;
        } catch (InputMismatchException exception) {
            System.out.println("Entrez un entier entre 1 et " + size);
            saisieUtilisateur.next();
            return choixEgaliter(size);

        }

    }


    private String ouverture(MotsPossible MP) {
        HashMap<Integer, String> bestOuverture = new HashMap<>();
        if (langue.equals(cheminAn)) {
            bestOuverture.put(2, "ho" + ANSI_RESET + " with an expected value of " + ANSI_gras + "27.489");
            bestOuverture.put(3, "eat" + ANSI_RESET + " with an expected value of " + ANSI_gras + "462.316");
            bestOuverture.put(4, "sale" + ANSI_RESET + " with an expected value of " + ANSI_gras + "2146.642");
            bestOuverture.put(5, "tares" + ANSI_RESET + " with an expected value of " + ANSI_gras + "4175.682");
        }

        if (langue.equals(cheminFR)) {
            bestOuverture.put(2, "au" + ANSI_RESET + " with an expected value of " + ANSI_gras + "48,374");
            bestOuverture.put(3, "aie" + ANSI_RESET + " with an expected value of " + ANSI_gras + "374,294");
            bestOuverture.put(4, "taie" + ANSI_RESET + " with an expected value of " + ANSI_gras + "1929,883");

        }
        int longeur = MP.getLongueur();
        String prop;
        if (bestOuverture.containsKey(longeur)) {
            prop = bestOuverture.get(longeur);
            messageChannel.sendMessage("Best opening : ").queue();
            messageChannel.sendMessage("> " + ANSI_RED + prop + ANSI_RESET + " eliminated words .").queue();
            return prop.substring(0, longeur);
        }
        prop = MP.random();
        if (longeur == 6) {
            System.out.println("Calculation of the expectation...");
            messageChannel.sendMessage("Calculation of the expectation...").queue();
            messageChannel.sendMessage("Proposal of a random opening :").queue();
            messageChannel.sendMessage("> " + ANSI_RED + prop + ANSI_RESET + " with an expected value of " + ANSI_gras + String.format("%.3f", MP.calculEsperance(prop)) + ANSI_RESET + " mots éliminé.").queue();
        } else {
            messageChannel.sendMessage("Proposal of a random opening :").queue();
            messageChannel.sendMessage("> " + ANSI_RED + prop + ANSI_RESET).queue();
        }
        //System.out.println(prop);
        return prop;
    }
}
