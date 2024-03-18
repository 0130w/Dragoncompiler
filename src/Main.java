import java.nio.file.Files;
import java.nio.file.Path;

public class Main
{    
    public static void main(String[] args){
        if (args.length == 0) {
            System.out.println("Usage: make run FILEPATH=path");
            return;
        }
        try {
            String content = Files.readString(Path.of(args[0]));
            System.out.println(content);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}