package WordleApp;

import java.util.Arrays;

public class Reponse {
    private final char[] proposition;
    private final Rep[] reponse;

    public Reponse(String proposition, Rep[] reponse) {
        this.proposition = proposition.toCharArray();
        this.reponse = reponse;
    }

    public Reponse(String motPropose, String rep) throws SizeReponseException {
        if (rep.length() != motPropose.length()) {
            throw new SizeReponseException("Mauvaise entré utilisateur, Attendu :" + motPropose.length() + " au lieux de :" + rep.length());
        } else {
            this.reponse = new Rep[motPropose.length()];
            this.proposition = motPropose.toCharArray();
            int i = 0;
            for (String r : rep.split("")) {
                switch (r) {
                    case "0" -> this.reponse[i] = Rep.NotInTheWorld;
                    case "1" -> this.reponse[i] = Rep.WrongSpot;
                    case "2" -> this.reponse[i] = Rep.Correct;
                    default -> throw new RuntimeException("Mauvaise valeur : " + r);
                }
                i++;
            }
        }
    }

    public static boolean verifRep(String rep) {
        return (rep.matches("[0-3]+"));
    }

    public char getProposition(int i) {
        return proposition[i];
    }

    public Rep getReponse(int i) {
        return reponse[i];
    }

    @Override
    public String toString() {
        return "Réponse : " + Arrays.toString(reponse);
    }

    enum Rep {
        Correct,
        WrongSpot,
        NotInTheWorld,
    }
}
