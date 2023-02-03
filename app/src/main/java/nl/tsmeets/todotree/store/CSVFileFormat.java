package nl.tsmeets.todotree.store;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CSVFileFormat {
    // FILE FORMAT:
    //   type; version
    //   hdr1; hdr2; hdr3;
    //   val1; val2; val3;
    //   val4; val5; val6;
    //
    // ESCAPING:
    //    ';' and '\n' are reserved!
    //    to escape ';' use '\,' and for '\n' use (literal) '\n'

    // Escape the string, making sure there are no ';' or '\n' present
    // to reverse use csv_string_read
    public static class Header {
       public String[] header;
    };

    private boolean is_reading = false;
    private boolean is_writing = false;

    // reading
    private int word_ix, line_ix;
    public String[] words;
    public String[] lines;

    // writing
    private StringBuilder out;
    private File file;

    // writing
    public void write_begin(File file, String... hdr) {
        assert this.is_writing == false;
        assert this.is_reading == false;
        this.is_writing = true;
        this.out = new StringBuilder();
        this.file = file;
        assert hdr.length > 0;

        // header
        for(String s : hdr)
            write_value(s);

        // done
        write_next();
    }

    public void write_value(String s) {
        // TODO: is there a faster way?
        for (char c : s.toCharArray()) {
            if (c == '\n') { out.append("\\n"); continue; }
            if (c == ';') { out.append("\\,"); continue; }
            if (c == '\\') { out.append("\\\\"); continue; }
            out.append(c);
        }
        out.append(';');
    }

    public void write_value(int v) {
        // NOTE: integers should not contain ';' nor '\n' so we don't need to escape
        out.append(v);
        out.append(';');
    }

    public void write_value(float v) {
        out.append(v);
        out.append(';');
    }

    public void write_value(boolean v) {
        out.append(v);
        out.append(';');
    }

    public void write_next() { out.append('\n'); }

    public void write_end() {
        assert out != null;
        assert is_writing == true;
        assert is_reading == false;

        // try to save atomically
        // first save to file.tmp, if successful move it over the new path.
        // This should reduce the chance that the file becomes corrupted if we crash while saving.
        File temp_save_file = new File(this.file.getPath() + ".tmp");
        try {
            FileOutputStream fos = new FileOutputStream(temp_save_file);
            fos.write(out.toString().getBytes(StandardCharsets.UTF_8));
            fos.close();
            temp_save_file.renameTo(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.out = null;
        this.is_writing = false;
        this.file = null;
    }




    // Reading
    public String[] read_begin(File f) {
        assert is_reading == false;
        assert is_writing == false;
        String s = Util.read_file_to_string(f);
        if(s == null) return null;
        this.lines = s.split("\n");
        this.words = null;
        this.word_ix = 0;
        this.line_ix = 0;

        if(!read_next()) return null;
        String[] hdr = new String[words.length];
        for (int i = 0; i < words.length; i++)
            hdr[i] = read_string();
        read_next();
        return hdr;
    }

    public boolean read_next() {
        // we expect a newline!
        if(this.line_ix == this.lines.length) {
            return false;
        }

        this.word_ix = 0;
        this.words = this.lines[this.line_ix++].split(";");
        return true;
    }

    public String read_string() {
        if(this.word_ix >= this.words.length)
            return null;

        if(this.words == null)
            return null;

        return csv_string_read(this.words[this.word_ix++]);
    }

    public int read_int() {
        return Integer.parseInt(read_string());
    }

    public float read_float() {
        return Float.parseFloat(read_string());
    }

    public boolean read_bool() {
        return Boolean.parseBoolean(read_string());
    }

    public void read_end() {
        this.word_ix = 0;
        this.line_ix = 0;
        this.words = null;
        this.lines = null;
    }

    // Parse the escaped string, converting the escaped characters back.
    // to reverse use csv_string_write
    private String csv_string_read(String s) {
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
}
