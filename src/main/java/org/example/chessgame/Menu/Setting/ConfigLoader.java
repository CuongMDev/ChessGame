package org.example.chessgame.Menu.Setting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private Properties properties = new Properties();

    private static final String CONFIG_FOLDER = "configs/";
    private static final String CONFIG_FILE = "configs.properties";

    public ConfigLoader() {
        File folder = new File(CONFIG_FOLDER);
        if (!folder.exists()) folder.mkdirs();
        File file = new File(CONFIG_FOLDER.concat(CONFIG_FILE));
        if (!file.exists()) {
            try {
                file.createNewFile();
                System.out.println("Create New Properties File");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            properties.load(new FileInputStream(CONFIG_FOLDER.concat(CONFIG_FILE)));
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public Boolean getBoolean(String key) {
        String value = get(key);
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    public Double getDouble(String key) {
        String value = get(key);
        if (value == null) {
            return null;
        }
        return Double.parseDouble(value);
    }

    public Integer getInt(String key) {
        String value = get(key);
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value);
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
    }

    public void save() {
        try (FileOutputStream out = new FileOutputStream(CONFIG_FOLDER.concat(CONFIG_FILE))) {
            properties.store(out, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}