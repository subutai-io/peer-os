package od.pages;

import java.io.*;

public class ReaderFromFile {

    public static String readDataFromFile(String fileName) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        File file = new File(fileName);
        try {
            try (BufferedReader in = new BufferedReader(new FileReader(file.getAbsoluteFile()))) {
                String s;
                while ((s = in.readLine()) != null) {
                    sb.append(s);
                    sb.append("\n");
                }
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}
