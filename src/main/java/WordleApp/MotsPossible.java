package WordleApp;

import discordBot.Bot;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.io.File;
import java.util.*;

public class MotsPossible {

    public static final String GRAS = "**";

    private final int longueur;
    private final List<Reponse.Rep[]> possibiliter;
    private List<String> motsPossible;

    private final MessageChannel messageChannel;


    private int div;


    public MotsPossible(int longueur, String langue, MessageChannel messageChannel) {
        File doc = new File(langue);
        Mots mots = new Mots(doc);
        this.longueur = longueur;
        motsPossible = mots.motsde(longueur);
        this.possibiliter = possibiliter();
        this.messageChannel=messageChannel;
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
            Reponse.Rep[] tab = new Reponse.Rep[longueur];
            tab[0] = rep;
            possibiliterMem.add(tab);
        }
        for (int i = 1; i < longueur; i++) {
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


    public void elimination(Reponse reponse) {
        int avant = motsPossible.size();
        this.motsPossible = elimine(reponse);
        int mtm = motsPossible.size();
        int dif = avant - mtm;

        if (mtm==1) {
            messageChannel.sendMessage("Congratulations! The word we are looking for is :").queue();
            messageChannel.sendMessage("> "+GRAS+motsPossible.get(0)+GRAS).queue();
        } else if (mtm==0){
            messageChannel.sendMessage("There is no more possible word, we had a problem sorry").queue(); //exit
        } else {
            messageChannel.sendMessage(GRAS+dif+GRAS+" words were removed, there are still " +mtm+" possible words.").queue();
        }
    }

    private double proba(String mot, Reponse.Rep[] reponse) {
        int probaXsize = elimine(new Reponse(mot, reponse)).size();
        int nbElimination = motsPossible.size() - probaXsize;
        int mult = probaXsize * nbElimination;
        //if(mult!=0) div=div+nbElimination;
        div = div + probaXsize;
        //System.out.print(mult+"="+probaXsize+"*"+nbElimination+",");

        return mult;
    }

    private List<String> elimine(Reponse reponse) {
        List<String> newMotsPossible = new ArrayList<>(motsPossible);
        List<Character> letresPresente = new ArrayList<>();
        for (int i = 0; i < longueur; i++) {
            Reponse.Rep rep = reponse.getReponse(i);
            char c = reponse.getProposition(i);
            if (rep == Reponse.Rep.Correct || rep == Reponse.Rep.WrongSpot) letresPresente.add(c);
        }

        for (int i = 0; i < longueur; i++) {
            Reponse.Rep rep = reponse.getReponse(i);
            char c = reponse.getProposition(i);
            if (rep == Reponse.Rep.Correct) {
                for (String str : motsPossible) {
                    if (str.charAt(i) != c) {
                        newMotsPossible.remove(str);
                    }
                }
            } else if (rep == Reponse.Rep.WrongSpot) {
                for (String str : motsPossible) {
                    if (!(str.contains(String.valueOf(c)))) {
                        newMotsPossible.remove(str);
                    }
                    for (int index = str.indexOf(c);
                         index >= 0;
                         index = str.indexOf(c, index + 1)) {
                        if (index == i) {
                            newMotsPossible.remove(str);
                        }
                    }
                }
            } else if (rep == Reponse.Rep.NotInTheWorld) {
                for (String str : motsPossible) {
                    if (countOccurences(str, c, 0) > Collections.frequency(letresPresente, c)) {
                        newMotsPossible.remove(str);

                    }
                }
            }


        }

        return newMotsPossible;

    }

    public double calculEsperance(String mot) {
        double Esperance = 0;
        div = 0;
        for (Reponse.Rep[] rep : possibiliter) {
            Esperance = Esperance + proba(mot, rep);
        }
        return Esperance / div;
    }

    public List<String> choix() {
        List<String> listMeilleur = new ArrayList<>();
        double esp = 0;
        HashMap<String, Double> dic = new HashMap<>();
        int i = 1;
        int T = motsPossible.size();
        for (String str : motsPossible) {
            double E = calculEsperance(str);
            //messageChannel.sendMessage("[" + i + "/" + T + "] "+str + " with a score of : " + String.format("%.3f",E)).queue();
            i++;
            dic.put(str, E);
            if (esp < E) {
                esp = E;
            }
        }

        for (Map.Entry<String, Double> e : dic.entrySet()) {
            if (e.getValue() == esp) {
                listMeilleur.add(e.getKey());
            }

        }
        if (listMeilleur.size() == 1) {
            messageChannel.sendMessage("The best entry is :").queue();
            messageChannel.sendMessage("> "+GRAS+ listMeilleur.get(0)+GRAS).queue();
            messageChannel.sendMessage("with an expectation of " + GRAS + String.format("%(.1f", esp) + GRAS + " words removed.").queue();
        } else {
            messageChannel.sendMessage("Each of them has an expectation of " + GRAS + String.format("%(.1f", esp) + GRAS +" words removed.").queue();
        }
        return listMeilleur;
    }

    public String random() {
        return motsPossible.get((int) (Math.random() * ((motsPossible.size()))));
        //return "chugs";
    }

    public int getLongueur() {
        return longueur;
    }

    public List<String> getMotsPossible() {
        return motsPossible;
    }

    public List<Reponse.Rep[]> getPossibiliter() {
        return possibiliter;
    }
}