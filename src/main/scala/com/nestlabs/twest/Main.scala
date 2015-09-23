/**
 *  Copyright 2014 Nest Labs Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.nestlabs.twest

import java.util.Properties
import java.util.concurrent.LinkedBlockingQueue
import com.twitter.hbc.core.event.{ConnectionEvent, HttpResponseEvent, Event}
import com.twitter.hbc.core.{Constants, HttpHosts}
import com.twitter.hbc.core.endpoint.UserstreamEndpoint
import com.twitter.hbc.httpclient.auth.OAuth1
import com.twitter.hbc.ClientBuilder
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import com.fasterxml.jackson.databind.ObjectMapper
import twitter4j._
import java.io.{InputStream, File}
import twitter4j.auth.{AccessToken, RequestToken, OAuth2Token}
import java.lang
import twitter4j.conf.ConfigurationBuilder
import akka.actor.{Props, ActorSystem}

object Main {
  def main(args: Array[String]) {
    // get our credentials
    val props = new Properties()
    props.load(this.getClass.getClassLoader.getResourceAsStream("credentials.txt"))
    val firebaseURL = props.getProperty("firebase-url")
    val twitterToken = props.getProperty("twitter-token")
    val twitterTokenSecret = props.getProperty("twitter-token-secret")
    val twitterApiKey = props.getProperty("twitter-api-key")
    val twitterApiSecret = props.getProperty("twitter-api-secret")
    val nestToken = props.getProperty("nest-token")

    // create a top level actor to connect the Nest API with the Twitter REST and Streaming APIs
    val system = ActorSystem("mySystem")
    system.actorOf(Props(new TopActor(firebaseURL, twitterApiKey, twitterApiSecret, twitterToken, twitterTokenSecret, nestToken)))
  }
}
