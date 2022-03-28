package WordleApp;

import java.io.File;
import java.util.*;

public class MotsPossible {

    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_gras = "\u001B[1m";

    private final int longueur;
    private final List<Reponse.Rep[]> possibiliter;
    private List<String> motsPossible;


    private int div;


    public MotsPossible(int longueur, String langue) {
        File doc = new File(langue);
        Mots mots = new Mots(doc);
        this.longueur = longueur;
        motsPossible = mots.motsde(longueur);
        this.possibiliter = possibiliter();

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

    public String premier() {
        System.out.println("Il y a " + motsPossible.size() + " mots posible restants");
        System.out.println(motsPossible);
        List<HashMap<Character, Integer>> stat = new ArrayList<>(longueur);
        for (int i = 0; i < longueur; i++) {
            stat.add(new HashMap<>());
            for (String mot : motsPossible) {
                char c = mot.toCharArray()[i];
                HashMap<Character, Integer> dic = stat.get(i);
                if (!(dic.containsKey(c))) {
                    dic.put(c, 1);
                } else {
                    dic.put(c, dic.get(c) + 1);
                }
            }
        }
        for (HashMap<Character, Integer> dic : stat) {
            System.out.println(dic);
        }


        return null;
    }

    public void elimination(Reponse reponse) {
        int avant = motsPossible.size();
        this.motsPossible = elimine(reponse);
        int mtm = motsPossible.size();
        int dif = avant - mtm;
        System.out.println("On avait " + avant + " mots possible et on en élimine " + ANSI_gras + dif + ANSI_RESET + ".");
        System.out.println("On a donc " + mtm + " mots restants :");
        if (motsPossible.size() == 1) {
            System.out.println(ANSI_GREEN + motsPossible.get(0) + ANSI_RESET);
        }
        //System.out.println(motsPossible);
    }

    private double proba(String mot, Reponse.Rep[] reponse) {
        int probaXsize = elimine(new Reponse(mot, reponse)).size();
        int nbElimination = motsPossible.size() - probaXsize;
        int mult = probaXsize * nbElimination;
        //if(mult!=0) div=div+nbElimination;
        div = div + probaXsize;
        //System.out.print(mult+"="+probaXsize+"*"+nbElimination+",");

        return (double) mult;
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
        //System.out.print("somme= "+div);
        return Esperance / div;
    }

    public List<String> choix() {
        List<String> listMeilleur = new ArrayList<>();
        double con = 0;
        HashMap<String, Double> dic = new HashMap<>();
        int i = 1;
        int T = motsPossible.size();
        for (String str : motsPossible) {
            double E = calculEsperance(str);
            System.out.print("[" + i + "/" + T + "] ");
            System.out.println(str + " avec un score de: " + E);
            i++;
            dic.put(str, E);
            double max = E;
            if (con < max) {
                con = max;
            }
        }

        for (Map.Entry<String, Double> e : dic.entrySet()) {
            if (e.getValue() == con) {
                listMeilleur.add(e.getKey());
            }

        }
        if (listMeilleur.size() == 1) {
            System.out.print("La meilleure proposition est ");
            System.out.print(ANSI_RED + listMeilleur.get(0) + " " + ANSI_RESET);
        } else {
            System.out.println("Les mots qui retirent le plus sont : ");
            for (int j = 0; j < listMeilleur.size(); j++) {
                System.out.println((j + 1) + "- " + listMeilleur.get(j));
            }
        }
        double esp = (double) con;
        System.out.println("avec une espérance de " + ANSI_gras + String.format("%.3f", esp) + ANSI_RESET + " mots éliminés.");
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