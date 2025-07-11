package org.example.chessgame.Menu.Setting;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private Properties properties = new Properties();

    public ConfigLoader() {
        try {
            properties.load(new FileInputStream("config.properties"));
        } catch (NullPointerException | IOException e) {
            System.out.println("Create New Properties File");
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
        try (FileOutputStream out = new FileOutputStream("config.properties")) {
            properties.store(out, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}