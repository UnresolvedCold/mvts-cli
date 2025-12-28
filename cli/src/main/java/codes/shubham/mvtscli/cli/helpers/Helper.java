package codes.shubham.mvtscli.cli.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Helper {
  public static ObjectMapper getObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT, false);
    return mapper;
  }
}
