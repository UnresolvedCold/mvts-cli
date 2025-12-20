package codes.shubham.mvtscli;

import java.io.FileInputStream;
import java.util.Properties;

public enum ApplicationProperties {
  MVTS_JAR_FILE_LOCATION("MVTS_JAR_FILE_LOCATION", "/home/shubham.codes/");

  private String key;
  private String value;

  static {
    Properties properties = new Properties();

    String envFileLocation = System.getenv("MVTS_CLI_PROPERTIES_FILE");

    if (envFileLocation == null) {
      envFileLocation = "config/mvts.cli.properties";
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

  public void setValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
