package WordleApp;

import net.dv8tion.jda.api.entities.MessageChannel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class MotsPossible {

    public static final String GRAS = "**";

    private final int size;
    private final List<Reponse.Rep[]> possibiliter;
    private final List<String> all;
    private List<String> motsPossible;

    private final MessageChannel messageChannel;


    //private int div;


    public MotsPossible(int size, String langue, MessageChannel messageChannel) {
        List<String> all1;
        this.size = size;
        try {
            all1 = Files.lines(Paths.get(langue)).filter(mot -> mot.length() == size).collect(Collectors.toList());
            motsPossible = all1;
        } catch (IOException e) {
            e.printStackTrace();
            all1 = null;
        }
        all = all1;
        this.possibiliter = possibiliter();
        this.messageChannel = messageChannel;
    }


    private static int countOccurences(String someString, char searchedChar, int index) {
        if (index >= someString.length()) {
            return 0;
        }

        int count = someString.charAt(index) == searchedChar ? 1 : 0;
        return count + countOccurences(
                someString, searchedChar, index + 1);
    }

    private List<Reponse.Rep[]> possibiliter() {
        List<Reponse.Rep[]> possibiliter;
        List<Reponse.Rep[]> possibiliterMem = new ArrayList<>();
        for (Reponse.Rep rep : Reponse.Rep.values()) {
            Reponse.Rep[] tab = new Reponse.Rep[size];
            tab[0] = rep;
            possibiliterMem.add(tab);
        }
        for (int i = 1; i < size; i++) {
            possibiliter = new ArrayList<>();
            for (Reponse.Rep[] tab : possibiliterMem) {
                for (Reponse.Rep rep : Reponse.Rep.values()) {
                    Reponse.Rep[] tabNew = tab.clone();
                    tabNew[i] = rep;
                    possibiliter.add(tabNew);
                }
            }
            possibiliterMem = possibiliter;
        }

        possibiliter = possibiliterMem;
        return possibiliter;
    }


    public int elimination(Reponse reponse) {
        int avant = motsPossible.size();
        this.motsPossible = elimine(reponse);
        int mtm = motsPossible.size();
        int dif = avant - mtm;

        if (mtm == 1) {
            messageChannel.sendMessage("Congratulations! The word we are looking for is :").queue();
            messageChannel.sendMessage("> " + GRAS + motsPossible.get(0) + GRAS).queue();
        } else if (mtm == 0) {
            messageChannel.sendMessage("There is no more possible word, we had a problem sorry").queue(); //exit
        } else {
            messageChannel.sendMessage(GRAS + dif + GRAS + " words were removed, there are still " + mtm + " possible words.").queue();
        }
        return mtm;
    }


    private List<String> elimine(Reponse reponse) {
        List<Character> letresPresente = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Reponse.Rep rep = reponse.getReponse(i);
            char c = reponse.getProposition(i);
            if (rep == Reponse.Rep.Correct || rep == Reponse.Rep.WrongSpot) letresPresente.add(c);
        }

        class LocalCollector {
            private Collector<String, ?, List<String>> getCollector(int i) {
                if (i == size) return Collectors.toList();
                else return Collectors.filtering(getPredicate(i), getCollector(i + 1));
            }

            private Predicate<String> getPredicate(int i) {
                Reponse.Rep rep = reponse.getReponse(i);
                char c = reponse.getProposition(i);
                return mot -> {
                    if (rep == Reponse.Rep.Correct) {
                        return mot.charAt(i) == c;
                    }
                    if (rep == Reponse.Rep.WrongSpot) {
                        int index = mot.indexOf(c);
                        if (index == -1) return false;
                        for (; index >= 0; index = mot.indexOf(c, index + 1)) { //list of index better
                            if (index == i) return false;
                        }
                    } else return mot.chars().filter(ch -> ch == c).count() <= Collections.frequency(letresPresente, c);
                    return true;
                };
            }
        }
        return motsPossible.stream().collect(new LocalCollector().getCollector(0));
    }


    public double calculEsperance(String mot) {
        double Esperance = 0;
        int div = 0;
        for (Reponse.Rep[] rep : possibiliter) {
            int probaXsize = elimine(new Reponse(mot, rep)).size();
            int nbElimination = motsPossible.size() - probaXsize;
            int mult = probaXsize * nbElimination;
            Esperance = Esperance + mult;
            div = div + probaXsize;

        }
        return Esperance / div;


    }

    public List<String> choix() {
        //long messageId = messageChannel.sendMessage("Calculation...").complete().getIdLong();
        long startTime = System.nanoTime();
        Map<String, Double> mapStream = all.parallelStream()
                .collect(Collectors.toUnmodifiableMap(Function.identity(), mot -> {
                    double esp = calculEsperance(mot);
                    System.out.println(mot+" : "+esp);
                    return esp;
                }));
        Double espMax =
                Collections.max(mapStream.values());
        List<String> listMeilleur = mapStream.entrySet().parallelStream().filter(entry -> Objects.equals(entry.getValue(), espMax)).map(Map.Entry::getKey).toList();
        long endTime = System.nanoTime();
        System.out.println((endTime - startTime) + " ns : Parallel Stream " + listMeilleur.size() + " " + listMeilleur); //why it doesn't work
/*
        int nb = listMeilleur.size();
        if (nb > 1) {
            List<String> toptop = listMeilleur.stream().filter(mot -> motsPossible.contains(mot)).toList();
            if (toptop.size() > 0) {
                System.out.println("oui");
                listMeilleur = toptop;
                nb = listMeilleur.size();
            }
        }

        if (nb == 1) {
            String bestEntry = listMeilleur.get(0);

            if (motsPossible.contains(bestEntry)) {
                messageChannel.editMessageById(messageId, "Maybe the right one !").queue();
            } else {
                messageChannel.editMessageById(messageId, "To eliminate as many possibilities!").queue();
            }
            messageChannel.sendMessage("The best entry is :").queue();
            messageChannel.sendMessage("> " + GRAS + bestEntry + GRAS).queue();
            messageChannel.sendMessage("with an expectation of " + GRAS + String.format("%(.1f", espMax) + GRAS + " words removed.").queue();
        } else {
            messageChannel.editMessageById(messageId, nb + " proposals have an expectation of " + GRAS + String.format("%(.1f", espMax) + GRAS + " words removed.").queue();
        }
        */

        return listMeilleur;
    }

    public String random() {
        return motsPossible.get((int) (Math.random() * ((motsPossible.size()))));
        //return "chugs";
    }

    public int getSize() {
        return size;
    }

    public List<String> getMotsPossible() {
        return motsPossible;
    }

    public List<Reponse.Rep[]> getPossibiliter() {
        return possibiliter;
    }

    public void removeFirstLetter(char c) {
        this.motsPossible = motsPossible.stream().filter(mot -> mot.charAt(0) == c).toList();
    }
}