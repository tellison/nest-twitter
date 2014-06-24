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

import akka.actor.{Props, Actor}
import java.util.concurrent.{Executors, ThreadPoolExecutor, LinkedBlockingQueue}
import com.twitter.hbc.core.event.Event
import com.twitter.hbc.core.{Constants, HttpHosts}
import com.twitter.hbc.core.endpoint.UserstreamEndpoint
import com.twitter.hbc.httpclient.auth.OAuth1
import org.codehaus.jackson.JsonNode
import com.twitter.hbc.ClientBuilder
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import scala.util.Random

object StreamActor {
  def props(apiKey: String,
            apiSecret: String,
            token: String,
            tokenSecret: String): Props = Props(new StreamActor(apiKey, apiSecret, token, tokenSecret))
}

/**
 * an actor that listens to the streaming API listening for ETA requests via DM
 * @param apiKey
 * @param apiSecret
 * @param token
 * @param tokenSecret
 */
class StreamActor(apiKey: String, apiSecret: String, token: String, tokenSecret: String) extends Actor {
  /** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
  val msgQueue = new LinkedBlockingQueue[String](100000)
  val eventQueue = new LinkedBlockingQueue[Event](1000)

  /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
  val hosebirdHosts = new HttpHosts(Constants.USERSTREAM_HOST)
  val hosebirdEndpoint = new UserstreamEndpoint()
  // These secrets should be read from a config file
  val hosebirdAuth = new OAuth1(apiKey, apiSecret, token, tokenSecret)

  val  builder = new ClientBuilder()
    .name("Hosebird-Client-01")                              // optional: mainly for the logs
    .hosts(hosebirdHosts)
    .authentication(hosebirdAuth)
    .endpoint(hosebirdEndpoint)
    .processor(new StringDelimitedProcessor(msgQueue))
    .eventMessageQueue(eventQueue);

  val client = builder.build()

  val mapper = new ObjectMapper()

  val rand = new Random()

  client.connect()

  def receive = {
    case e: Event => {
      println("got hosebird event: " + e)
      println(e.getMessage)
    }
    case j: ObjectNode => {
      // if we get a direct message check it for an ETA mesage
      if (j.get("direct_message") != null) {
        val text = j.get("direct_message").get("text").asText()
        val sender = j.get("direct_message").get("sender").get("id").asLong()
        if (text.startsWith("home in")) {
          try {
            val eta = text.substring(8).toInt
            // this is an ETA message, send our parent coordinator a message
            context.parent ! ETA(sender, "tweet-trip-" + rand.nextInt(20000), eta)
          } catch {
            case e: Exception => println("failed to parse ETA DM: " + e)
          }
        }
      }
    }
    case m => {
      println("got unknown msg: %s (%s)".format(m, m.getClass.getName))
    }
  }

  val events = new Runnable {
    override def run() {
      while(!client.isDone) {
        println("waiting for a hb event")
        val evt = eventQueue.take()
        self ! evt
      }
    }
  }

  val msgs = new Runnable {
    override def run() {
      while(!client.isDone) {
        println("waiting for a hb message")
        val msg = msgQueue.take()
        try {
          val tree = mapper.readTree(msg)
          self ! tree
        } catch {
          case e: Exception => println("error parsing json: " + e)
        }
      }
    }
  }

  val pool = Executors.newFixedThreadPool(2)
  pool.submit(events)
  pool.submit(msgs)

}
