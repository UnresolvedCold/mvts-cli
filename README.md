# MVTS CLI

> ⚠️This tool is in alpha stage. Use it at your own risk.

A set of companion tools to help you with MVTS related stuffs.

## Installation

### Shell installation

You can install the released binary on your machine simply by downloading the released jar file. 
You will need to have java 21 (or above) installed on your machine for running mvts-cli.

The below script will download the latest jar and place it in your `/usr/local/bin` folder.
You can add this path to your `PATH` environment variable if it's not already there to access `mvts-cli` globally.

```bash
curl -fsSL https://raw.githubusercontent.com/unresolvedcold/mvts-cli/main/install.sh | bash
```

### Build from source

You can install `mvts-cli` by building the source. 
Clone the repository and simply run `mvn clean package`.

You will need java 21 (or above) and maven installed on your machine.

```bash
git clone git@github.com:UnresolvedCold/mvts-cli.git
cd mvts-cli
mvn clean package -DskipTests
```

## Configuration

You need to configure `mvts-cli` to read your logs and app files.
By default, `mvts-cli` reads logs from relative path `data/logs` directory. You may want to change this path to point to your actual logs directory. This can be done with `LOG_DIR` property in config file.

By default, all the configurations are read from `~/.config/mvts/mvts.cli.properties/` file.
You can change the location of config file by exposing an environment variable `MVTS_CLI_PROPERTIES_FILE`.

By default, the indexer used the directory `~/.mvts/` to store the index files.
You can delete this directory to clear all the indexes, if required.

## Utilities

### Search

The simplest way to get message and output json for a request id is by using the search command.
It will go through the log files and find the json for your request id. 
Once found, it will also index the request id for faster searches. 

To get the message for a request id, you can simpy use `message` or just `m` subcommand. 

```bash
mvts-cli search message <request-id>
mvts-cli search m <request-id>
```

To get the output json, use `output` or just `o` subcommand.

```bash
mvts-cli search output <request-id>
mvts-cli search o <request-id>
```

You can also filter the logs as per some regex, use `regex` or just `r` subcommand.

> You can omit request-id if you want to search all the logs for regex pattern

```bash
mvts-cli search regex <request-id> -r <regex-pattern>
mvts-cli search r <request-id> -r <regex-pattern>
```

By default, search will be done for active log file (scheduler.log).
You can specify the dates for which you want to search and relevant log files will be searched.

```bash
mvts-cli search message <request-id> --dates 2025-12-12 2025-12-13
```

### Json query

Json query, as the name suggests, allows you to run queries on your message and output.

There are 2 kinds of queries you can run.
1. Jmespath queries - These queries are run using [jmespath](https://github.com/json-path/JsonPath) syntax.
2. Recipies - These are more complex queries which can have multiple steps and conditions.

#### Jmespath queries

To run a Jmespath query, you can simply call the `jmespath` or just `j` subcommand with your request id and query.

> If you don't provide ids, it will run the query on your latest worked request ids.

```bash
mvts-cli json jmespath <query> -ids <request-id-1> <request-id-2> ...
mvts-cli json j <query> -ids <request-id-1> <request-id-2> ...
```

All the queries will we executed on both message and output json by default. 

```bash
mvts-cli json jmespath '$.task_list[0].task_key' -ids 3RD4xOJwSCS1lP7yMjGLwQ== --dates 2025-12-26

[ 
  {
    "request_id" : "3RD4xOJwSCS1lP7yMjGLwQ==",
    "message" : {
      "result" : "16cbb0c4-1246-4285-a965-3bcadd06526d"
    },
    "output" : { }
  } 
]
```

If you want only to run query for message, you can exclude output using `-eo` flag.
And similarly, you can exclude message using `-em` flag.

> Excluding the output or message will still print the output and message fields, but they will be empty.

If you are concern with specific fields in the queried result, you can use `-o` and provide a Jmespath query to filter the result.

```bash
mvts-cli json jmespath '$.task_list[0].task_key' -ids 3RD4xOJwSCS1lP7yMjGLwQ== --dates 2025-12-26 -o '$[*].message'

[ 
  {
    "result" : "16cbb0c4-1246-4285-a965-3bcadd06526d"
  } 
]
```

#### Recipies

Recipies are more complex queries. 
At the time of writing this document, there is only one recipie available - `task`.

The `task` recipie allows you to get details about tasks in your MVTS requests.
It fetches the task details, transport entity details and relay point details from message and assignment for the id in output.  

> You can implement your own recipies by implementing the `IQueryHandler` interface. 

```bash
mvts-cli json recipie task -p 16cbb0c4-1246-4285-a965-3bcadd06526d

[ {
  "request_id" : "3RD4xOJwSCS1lP7yMjGLwQ==",
  "message" : {
    "request_id" : "3RD4xOJwSCS1lP7yMjGLwQ==",
    "task_list" : [ {
      "aisle_info" : {
        "aisle_coordinate" : null,
        "aisle_id" : null
      },
      "assigned_ranger_id" : 50,
      "destination" : {
        "x" : 1,
        "y" : 80
      },
      "destination_id" : 21,
      "destination_type" : "charger",
      "rtr_ids" : [ ],
      "serviced_bins" : [ ],
      "serviced_orders" : [ ],
      "status" : "to_be_assigned",
      "task_key" : "16cbb0c4-1246-4285-a965-3bcadd06526d",
      "task_subtype" : "chargetask",
      "task_type" : "chargetask",
      "transport_entity_id" : null,
      "transport_entity_type" : null
    } ],
    "transport_entity_list" : [ ],
    "relay_point_list" : [ ]
  },
  "output" : {
    "request_id" : "3RD4xOJwSCS1lP7yMjGLwQ==",
    "assignments" : [ {
      "serviced_bins" : [ ],
      "serviced_orders" : [ ],
      "assigned_ranger_id" : 50,
      "auxiliary_bot_list" : [ ],
      "task_key" : "16cbb0c4-1246-4285-a965-3bcadd06526d",
      "task_type" : "chargetask",
      "transport_entity_type" : null,
      "transport_entity_id" : null,
      "dock_pps_id" : 0,
      "destination_id" : null,
      "dock_sequence_at_pps" : 0,
      "entity_drop_sequence" : 1,
      "entity_pick_sequence" : 1,
      "ranger_dock_coordinate" : null,
      "ranger_start_time" : 1766514271289,
      "operator_start_time" : null,
      "operator_end_time" : null,
      "ranger_available_start_time" : null,
      "task_subtype" : "unknown",
      "aisle_info" : {
        "aisle_id" : -1,
        "aisle_coordinate" : [ 0, 0 ]
      },
      "is_reordered" : false,
      "original_planned_time" : 0,
      "last_replanned_time" : 0,
      "ranger_group_task_key" : null,
      "mvts_predicted_bot_arrival_time" : null,
      "associated_task_list" : [ ]
    } ]
  }
} ]
```

### Index

With search you can only index the searched request ids. 
Sometimes you need to search huge number of request ids which is a very slow task.
You can index all the log files using the index command.

> Indexing huge log files can take a lot of time and disk space.

The below command will start indexing all the log files in the logs directory in certain intervals.

```bash
mvts-cli index --interval <time perios in seconds>
```

