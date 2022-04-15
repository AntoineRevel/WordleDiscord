package WordleApp.ressources;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

public class Propre {
    public static void main(String[] args) throws IOException {
        FileWriter out = new FileWriter("src/main/java/WordleApp/ressources/motsFR.txt");
        Files.lines(Paths.get("src/main/java/WordleApp/ressources/motsFR.txt")).map(mot -> {
            System.out.println(mot);
            return mot.toLowerCase(Locale.ROOT);})
                .forEach((mot) -> {
                try {
                    out.write(mot + System.getProperty("line.separator"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            out.close();
        }
    }

/*map(StringUtils::stripAccents).distinct()
                .filter(mot -> {
                    if(mot.contains("-")){
                        System.out.println(mot);
                        return false;
                    } else return true;
                })*/
