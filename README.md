# Twitter/Nest

Sample server-to-server integration between Nest and Twitter.

Allows you to pair up a Twitter account representing a structure with the user's Nest account. Uses a top level actor (`TopActor`) to coordinate updates between a Twitter REST Actor (used for posting updates and DMs), a Twitter Streaming Actor (for subscribing to DMs)
and a Nest actor (`NestActor`) for listening to Firebase updates.

## Install

To get started you'll need a [Twitter API key / access token][twitter-keys], and a [Nest access token][nest-keys].

Copy `src/main/resources/credentials-sample.txt` to `src/main/resources/credentials.txt`, fill in the placeholder values.

## Building

Builds are done with [Maven][maven] which is available on most platforms. You can compile the code by running:

``` sh
mvn compile
```

The `pom.xml` file in the root is a Maven project definition and can be opened directly as a project by most Java IDEs.

## Running

After you've compiled you can run the application by running the following:

``` sh
mvn exec:java -Dexec.mainClass=com.nestlabs.twest.Main
```

Assuming your config is set up (as described earlier) you should see messages logged to standard output.

## Contributing

Contributions are always welcome and highly encouraged.

See [CONTRIBUTING](CONTRIBUTING.md) for more information on how to get started.

## License

Apache 2.0 - See [LICENSE](LICENSE) for more information.

[maven]: http://maven.apache.org/
[twitter-keys]: https://dev.twitter.com
[nest-keys]: https://developer.nest.com
