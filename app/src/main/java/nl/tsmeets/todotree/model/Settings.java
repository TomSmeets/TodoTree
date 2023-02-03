package nl.tsmeets.todotree.model;

import java.io.File;

import nl.tsmeets.todotree.store.CSVFileFormat;
import nl.tsmeets.todotree.store.CSVFileFormat.Header;

public class Settings {
    // "Insert nodes on top"
    public boolean insert_top = false;

    // "Interface scale"
    public float ui_scale = 1.0f;

    // "Auto check parents"
    public boolean auto_check_parent = false;

    public void save(File file) {
        CSVFileFormat f = new CSVFileFormat();

        f.write_begin(file,  "key", "value");

        f.write_value("insert_top");
        f.write_value(insert_top);
        f.write_next();

        f.write_value("ui_scale");
        f.write_value(ui_scale);
        f.write_next();

        f.write_value("auto_check_parent");
        f.write_value(auto_check_parent);
        f.write_next();

        f.write_end();
    }

    public void load(File file) {
        CSVFileFormat f = new CSVFileFormat();
        String[] hdr = f.read_begin(file);
        if(hdr == null) return;

        assert hdr.length == 2;
        assert hdr[0].equals("key");
        assert hdr[1].equals("value");

        for(;;) {
            String key = f.read_string();
            assert key != null;
            if(key.equals("insert_top"))        insert_top = f.read_bool();
            if(key.equals("ui_scale"))          ui_scale = f.read_float();
            if(key.equals("auto_check_parent")) auto_check_parent = f.read_bool();
            if(!f.read_next()) break;
        }
        f.read_end();
    }
}
