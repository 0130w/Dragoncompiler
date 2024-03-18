import java.nio.file.Files;
import java.nio.file.Path;

public class Main
{    
    public static void main(String[] args){
        String file_arg = args[0];
        String[] file_parts = file_arg.split("=");
        if (file_parts.length > 1) {
            try {
                String content = Files.readString(Path.of(file_parts[1]));
                System.out.println(content);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        } else {
            System.out.println("Usage: make run FILEPATH=...");
        }
    }
}