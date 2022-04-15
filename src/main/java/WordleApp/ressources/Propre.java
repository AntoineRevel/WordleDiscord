package WordleApp.ressources;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Propre {
    public static void main(String[] args) throws IOException {
        FileWriter out=new FileWriter("src/main/java/WordleApp/ressources/motsFR.txt");
        Files.lines(Paths.get("src/main/java/WordleApp/ressources/motsFR.txt")).map(StringUtils::stripAccents).distinct()
                .forEach((mot)-> {
                    try {
                        out.write(mot+System.getProperty("line.separator"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        out.close();
    }
}
