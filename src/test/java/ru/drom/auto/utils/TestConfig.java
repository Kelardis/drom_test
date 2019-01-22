package ru.drom.auto.utils;

import lombok.Getter;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

@Getter
public class TestConfig {
    private static String DEFAULT_CONFIG = "test.properties";

    private String chromeWebDriverLocation;
    private String dromUserLogin;
    private String dromUserPassword;

    private static TestConfig instance = null;

    public static TestConfig getInstance() {
        if (TestConfig.instance == null) {
            TestConfig.instance = new TestConfig();
        }

        return TestConfig.instance;
    }

    private TestConfig() {
        try {
            Properties properties = new Properties();

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIG);

            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + DEFAULT_CONFIG + "' not found in the classpath");
            }

            this.chromeWebDriverLocation = properties.getProperty("chrome_webdriver_location");
            this.dromUserLogin = properties.getProperty("drom_user_login");
            this.dromUserPassword = properties.getProperty("drom_user_password");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
