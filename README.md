# MVTS CLI

A set of companion tools to help you with MVTS related stuffs. 

## Installation

```bash
# Right now, only supports linux x86_64
curl -fsSL https://raw.githubusercontent.com/unresolvedcold/mvts-cli/main/install.sh | bash
```

## Utilities

### Search

You can search the message and output json easily using `search` command.

```bash
mvts-cli search message <request-id>

mvts-cli search output <request-id>
```