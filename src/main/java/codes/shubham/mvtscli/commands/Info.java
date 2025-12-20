package codes.shubham.mvtscli.commands;

import codes.shubham.mvtscli.helpers.BuildInfo;
import picocli.CommandLine;

@CommandLine.Command(name = "info", description = "info")
public class Info implements Runnable {

  @Override
  public void run() {
    IO.println(BuildInfo.name());
    IO.println("Version: " + BuildInfo.version());
    IO.println("Vendor: " + BuildInfo.vendor());
  }
}
