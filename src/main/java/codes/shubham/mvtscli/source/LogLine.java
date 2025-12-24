package codes.shubham.mvtscli.source;

import codes.shubham.mvtscli.source.position.Position;

public record LogLine(
    String filePath,
    String line,
    Position position
) {}
