# spark-wrapper

A POM file that can be used to package all spark dependencies as a fat jar for environments where spark is not installed.

## Usage

1. Build the assembly jar.
    ```sh
    mvn clean install
    ```
2. Copy the JAR to classpath.
3. Run the applications.

    ```sh
    java $CONSUMER_JAVA_OPTS -cp spark-wrapper-assembly-0.0.1-SNAPSHOT.jar:rtpa-assembly-0.0.1-SNAPSHOT.jar com.waiyan.rtpa.consumer.ConsumerApp > ../logs/consumer.log 2>&1 & java $PRODUCER_JAVA_OPTS -cp rtpa-assembly-0.0.1-SNAPSHOT.jar com.waiyan.rtpa.producer.ProducerApp > ../logs/producer.log 2>&1
    ```
