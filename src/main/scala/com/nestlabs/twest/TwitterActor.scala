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
import twitter4j.conf.ConfigurationBuilder
import twitter4j.TwitterFactory
import java.text.SimpleDateFormat
import java.util.Date

/**
 * helper method to create a twitter actor with appropriate args
 */
object TwitterActor {
  def props(twitterApiKey: String,
            twitterApiSecret: String,
            twitterToken: String,
            twitterTokenSecret: String): Props = Props(new TwitterActor(twitterApiKey,
    twitterApiSecret,
    twitterToken,
    twitterTokenSecret))
}


/**
 * used to post status and send DMs based on nest responses
 */
class TwitterActor(twitterApiKey: String,
                   twitterApiSecret: String,
                   twitterToken: String,
                   twitterTokenSecret: String) extends Actor {
  val cb = new ConfigurationBuilder()
  cb.setDebugEnabled(true)
    .setOAuthConsumerKey(twitterApiKey)
    .setOAuthConsumerSecret(twitterApiSecret)
    .setOAuthAccessToken(twitterToken)
    .setOAuthAccessTokenSecret(twitterTokenSecret)

  val factory = new TwitterFactory(cb.build())
  val twitter = factory.getInstance()
  val dms = twitter.directMessages()

  val dateFormat = new SimpleDateFormat("h:mm a")
  def currentTime(): String = {
    dateFormat.format(new Date())
  }

  /**
   * listen for StructureUpdate, DeviceStateUpdate and ETAAck messages.
   * Format responses, post appropriately.
   */
  def receive = {
    case StructureUpdate(name, state) => {
      println("updating status on state change")
      doHome(state, name, currentTime)
    }
    case DeviceStateUpdate(structure, deviceType, location, statusType, status) => {
      println("updating status on device change")
      statusType match {
        case "smoke" => doSmoke(status, structure, location, currentTime)
        case "co" => doCO(status, structure, location, currentTime)
        case "battery" => doBattery(status, structure, location, currentTime)
        case "online" => doOnline(status, deviceType, structure, location, currentTime)
        case "target_temp" => doTargetTemp(status, structure, location, currentTime)
      }
    }
    case ETAAck(eta) => {
      val etaTime = System.currentTimeMillis() + eta.eta * 60 * 1000
      val d = new Date()
      d.setTime(etaTime)
      val dmText = "got it, setting ETA for %s".format(dateFormat.format(d))
      twitter.sendDirectMessage(eta.sender, dmText)
    }
    case e => "twitter actor got message " + e
  }

  def doHome(status: String, structure: String, currentTime: String) {
    if (status.toLowerCase.equals("home")) {
      twitter.updateStatus("%s welcomed somebody home at %s".format(structure, currentTime))
    } else {
      twitter.updateStatus("%s switched to away at %s".format(structure, currentTime))
    }
  }

  def doSmoke(status: String, structure: String, location: String, currentTime: String) {
    if (status.equals("ok")) {
      twitter.updateStatus("Smoke cleared in the %s at %s at %s".format(location, structure, currentTime))
    } else {
      twitter.updateStatus("%s: smoke detected in the %s at %s at %s".format(status.capitalize, location, structure, currentTime))
    }
  }

  def doBattery(status: String, structure: String, location: String, currentTime: String) {
    if (status.equals("ok")) {
      twitter.updateStatus("The batteries in the Protect in the %s at %s are ok".format(location, structure))
    } else {
      twitter.updateStatus("The batteries in the Protect in the %s at %s should be replaced".format(location, structure))
    }
  }

  def doCO(status: String, structure: String, location: String, currentTime: String) {
    if (status.equals("ok")) {
      twitter.updateStatus("CO cleared in the %s at %s at %s".format(location, structure, currentTime))
    } else {
      twitter.updateStatus("%s: CO detected in the %s at %s at %s".format(status.capitalize, location, structure, currentTime))
    }
  }

  def doOnline(status: String, deviceType: String, structure: String, location: String, currentTime: String) {
    if (status.equals("true")) {
      twitter.updateStatus("The %s in the %s at %s came back online at %s".format(deviceType, location, structure, currentTime))
    } else {
      twitter.updateStatus("The %s in the %s at %s went offline at %s".format(deviceType, location, structure, currentTime))
    }
  }
  def doTargetTemp(status: String, structure: String, location: String, currentTime: String) {
    twitter.updateStatus("The Thermostat in the %s at %s set a target temperature of %s at %s".format(location, structure, status, currentTime))
  }
}
