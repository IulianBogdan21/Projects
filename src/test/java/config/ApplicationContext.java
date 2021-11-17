package config;

import java.util.Properties;

public class ApplicationContext {
    private static final Properties PROPERTIES = Config.getProperties();

    public static Properties getPROPERTIES(){
        return PROPERTIES;
    }

    public static String getProperty(String key){
        return PROPERTIES.getProperty(key);
    }
}
