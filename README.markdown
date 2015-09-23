# Twitter/Nest

Sample server-server integration between Nest and Twitter. Allows you to pair up a twitter account representing a structure with the user's Nest account.
Uses a top level actor (TopActor) to coordinate updates between a Twitter REST Actor (used for posting updates and DMs), a Twitter Streaming Actor (for subscribing to DMs)
and a Nest actor (NestActor) for listening to firebase updates. To get started you'll need Twitter API keys and an access token (see https://dev.twitter.com), and a Nest
access token (see https://developer.nest.com). Copy src/main/resources/credentials-sample.txt to src/main/resources/credentials.txt, fill in the placeholder values and 
run Main.

## Building

Builds are done with Maven (http://maven.apache.org/) which is available on most platforms these days. You can compile code by running

`mvn compile`

The pom.xml file in the root is a Maven project definition and can be opened directly as a project by most Java IDEs.

## Running

After you've compiled you can run the application by running the following in a terminal window

`mvn exec:java -Dexec.mainClass=com.nestlabs.twest.Main`

Assuming configs are set up you should see messages logged to standard output.

## License
Copyright 2014 Nest Labs Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
