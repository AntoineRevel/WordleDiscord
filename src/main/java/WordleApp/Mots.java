package WordleApp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Mots {
    //fichier sélection
    private final List<String> mots;


    public Mots(File doc) {
        mots = new ArrayList<>();
        try {
            Scanner obj = new Scanner(doc);
            while (obj.hasNextLine()) {
                mots.add(obj.nextLine());
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void cleanMot(String file, String newfile) {
        File doc = new File(file);
        ArrayList<String> mots = new ArrayList<>();
        try {
            Scanner obj = new Scanner(doc);
            while (obj.hasNextLine()) {
                mots.add(obj.nextLine());
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            PrintWriter writer = new PrintWriter(newfile);
            String mem = "";
            for (String mot : mots) {
                mot = mot.replace('é', 'e');
                mot = mot.replace('è', 'e');
                mot = mot.replace('ê', 'e');
                mot = mot.replace('à', 'a');
                mot = mot.replace('â', 'a');
                if (!mot.contains("-") && !mot.equals(mem)) {
                    writer.println(mot);
                }
                mem = mot;

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public List<String> motsde(int l) {
        List<String> motsL = new ArrayList<>();
        for (String mot : mots) {
            if (mot.length() == l) {
                motsL.add(mot);
            }
        }
        return motsL;
    }


}
