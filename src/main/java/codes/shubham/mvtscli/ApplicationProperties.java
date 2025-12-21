package codes.shubham.mvtscli;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public enum ApplicationProperties {
  MVTS_JAR_FILE_LOCATION("MVTS_JAR_FILE_LOCATION", "/home/shubham.codes/"),
  LOG_DIR("LOG_DIR", "data/logs");

  private String key;
  private String value;
  private static String envFileLocation;

  static {
    loadApplicationProperties();
  }

  public static void loadApplicationProperties() {
    Properties properties = new Properties();

    envFileLocation = System.getenv("MVTS_CLI_PROPERTIES_FILE");

    if (envFileLocation == null) {
      envFileLocation = Paths.get(
          System.getProperty("user.home"),
          ".config",
          "mvts-cli",
          "mvts.cli.properties"
      ).toString();
    }

    // Create an empty properties file if it does not exist
    if (!(Paths.get(envFileLocation).toFile().exists())) {
      try {
        Files.createDirectories(Paths.get(envFileLocation).getParent());
        Files.createFile(Paths.get(envFileLocation));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    try {
      properties.load(new FileInputStream(envFileLocation));
    } catch (Exception e) {
      e.printStackTrace();
    }
    for (ApplicationProperties property : ApplicationProperties.values()) {
      if (properties.containsKey(property.key)) {
        property.value = properties.getProperty(property.key);
      }
    }
  }

  ApplicationProperties(String key, String value) {
    this.key = key;
    this.value = value;
  }

  // Set and write to file
  public void setValue(String value) {
    this.value = value;
  }

  public static String getEnvFileLocation() {
    return envFileLocation;
  }

  public String getValue() {
    return value;
  }
}
