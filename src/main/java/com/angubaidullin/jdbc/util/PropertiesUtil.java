package com.angubaidullin.jdbc.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesUtil {
    private PropertiesUtil() {}

    /*
    Для представляния свойств из файла есть специальный класс
     */
    private static final Properties PROPERTIES = new Properties();

    static {
        loadProps();
    }

    //Создадим публичный метод, который будет возвращать значение по ключу
    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

    private static void loadProps() {
        try (InputStream resourceAsStream = PropertiesUtil.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            PROPERTIES.load(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
