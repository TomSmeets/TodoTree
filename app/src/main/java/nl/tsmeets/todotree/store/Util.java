package nl.tsmeets.todotree.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Util {
    public static void write_csv_string(StringBuilder out, String s) {
        for (char c : s.toCharArray()) {
            if (c == '\n') { out.append("\\n"); continue; }
            if (c == ';') { out.append("\\,"); continue; }
            if (c == '\\') { out.append("\\\\"); continue; }
            out.append(c);
        }
    }

    public static String read_csv_string(String s) {
        StringBuilder out = new StringBuilder();
        boolean escape = false;
        for (char c : s.toCharArray()) {
            if(escape) {
                if(c == 'n')  out.append('\n');
                if(c == ',')  out.append(';');
                if(c == '\\') out.append('\\');
                escape = false;
                continue;
            }

            if(c == '\\') {
                escape = true;
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }

    public static void write_string_to_file(File f, String s) {
        try {
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(s.getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }
    }

    public static String read_file_to_string(File f) {
        try {
            FileInputStream fis = new FileInputStream(f);
            byte[] data = new byte[(int) f.length()];
            fis.read(data);
            fis.close();
            return new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }
}