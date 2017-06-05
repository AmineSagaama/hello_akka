package com.asagaama.actor

import akka.actor.{Actor, ActorSystem, Props}
import com.asagaama.actor.MusicPlayer.{StartMusic, StopMusic}

/**
  * Created by asagaama on 05/06/2017.
  */

// Music Controller Messages
object MusicController {

  def props = Props[MusicController]

  sealed trait ControllerMsg

  case object Play extends ControllerMsg

  case object Stop extends ControllerMsg

}


// Music Controller
class MusicController extends Actor {
  def receive = {
    case MusicController.Play => println("Music started......")
    case MusicController.Stop => println("Music stopped......")
  }
}

// Music Player Messages
object MusicPlayer {

  sealed trait PlayMsg

  case object StopMusic extends PlayMsg

  case object StartMusic extends PlayMsg

}

// Music Player
class MusicPlayer extends Actor {
  def receive = {
    case StopMusic => println("I don't want to stop music")
    case StartMusic => val controller = context.actorOf(MusicController.props, "controller")
      controller ! MusicController.Play
    case _ => println("Unknown Message")
  }
}

object Creation extends App {
  // Create the 'creation' actor system
  val system = ActorSystem("creation")
  // Create the 'MusicPlayer' actor
  val player = system.actorOf(Props[MusicPlayer], "player")

  // send StartMusic Message to Actor
  player ! StartMusic
}
