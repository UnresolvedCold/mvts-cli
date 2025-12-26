package codes.shubham.mvtscli;

import codes.shubham.mvtscli.commands.Index;
import codes.shubham.mvtscli.commands.JsonQuery;
import codes.shubham.mvtscli.commands.Search;
import picocli.CommandLine;

@CommandLine.Command(
    name = "mvts-cli",
    mixinStandardHelpOptions = true,
    description = "A MVTS companion tool with lots of features",
    subcommands = {
        Search.class,
        Index.class,
        JsonQuery.class
    }
)
public class Main implements Runnable {
    @Override
    public void run() {
        System.out.println("Use a subcommand. Try --help");
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
