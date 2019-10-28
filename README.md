# Mosquitto to HiveMQ Migration Tool

MQTT 5.0 and 3.1.1 compatible migration tool to migrate your persistent connections from Mosquitto to the HiveMQ Enterprise Broker.

## Features

- **All MQTT 3.1.1 and MQTT 5.0 features** are supported (except shared subscriptions, which are not directly persisted in Mosquitto)
- latest Mosquitto support (1.6.7) as well as all 1.6.* releases
- easy to use

### Prerequisites

The tool was created using Java 11.


### Quickstart

#### Download
There is a package prepared as release of the repository that can be  
downloaded. See [packages/1.0.0/mosquitto2hivemq-1.0.0.zip](Zip file of the current package) 

The Zip file contains a jar and the README.
After downloading the package can be unzipped and migration can start.

The simplest way to start the migration tool is typing:
```$ java -jar mosquitto2hivemq-1.0.0.jar -i <mosquitto.db>```
See also ``java -jar mosquitto2hivemq-1.0.0.jar --help``.

A timestamped folder will be created, which contains a handy backup file. You can then import this file via the HiveMQ Control Center under the Admin/Backup section.

### Building from source

To do a clean build, issue the following command:

`$ ./gradlew build`

This creates the **migration-tool.jar** which can then be executed.

#### Command Attributes

|Attribute                                         |Explanation                                                            |
|------------------------------------------------|-------------------------------------------------------------------------|
| ``-i`` | Enter the input Mosquitto database file which is needed for the migration.
| ``-o`` | Add a path to specify the output folder of the HiveMQ migration Folder.
| ``-k`` | Keep the XML Files created by the migration tool for further investigation.
| ``-v`` | Enable verbose mode to get more details during the migration.

See also ``java -jar mosquitto2hivemq-1.0.0.jar --help``

## License

See [LICENSE](LICENSE)
