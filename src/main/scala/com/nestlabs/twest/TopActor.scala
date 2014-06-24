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

import akka.actor.Actor

/**
 * indicates a change to a structure
 */
case class StructureUpdate(name: String, state: String)
/**
 * indicates a change to device status
 */
case class DeviceStateUpdate(structure: String, deviceType: String, location: String, statusType: String, status: String)
/**
 * models an ETA request
 */
case class ETA(sender: Long, tripID: String, eta: Int, acked: Boolean = false)
/**
 * indicates receipt of an ETA request
 */
case class ETAAck(eta: ETA)

/**
 * coordinates work between NestActor, StreamActor and TwitterActor instances.
 * when device/structure updates and ETA Acks come in send them to the Twitter Actor.
 * when ETA messages come in route them to the nest actor.
 */
class TopActor(firebaseURL: String, apiKey: String, apiSecret: String, token: String, tokenSecret: String, nestToken: String) extends Actor {
  val streamActor = context.actorOf(StreamActor.props(apiKey, apiSecret, token, tokenSecret))
  val twitterActor = context.actorOf(TwitterActor.props(apiKey, apiSecret, token, tokenSecret))
  val nestActor = context.actorOf(NestActor.props(nestToken, firebaseURL))

  def receive = {
    case upd:StructureUpdate => twitterActor ! upd
    case upd:DeviceStateUpdate => twitterActor ! upd
    case eta:ETA => nestActor ! eta
    case eta:ETAAck => twitterActor ! eta
    case m => println("top actor got " + m)
  }
}
