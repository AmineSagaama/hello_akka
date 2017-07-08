package com.asagaama.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}

/**
  * Created by asagaama on 05/06/2017.
  */
class Ares(athena: ActorRef) extends Actor {

  override def preStart() = {
    context.watch(athena)
  }

  override def postStop() = {
    println("Ares postStop...")
  }

  def receive = {
    case Terminated =>
      context.stop(self)
  }
}

class Athena extends Actor {
  def receive = {
    case msg =>
      println(s"Athena received ${msg}")
      context.stop(self)
  }
}


object Monitoring extends App {
  // Create the 'monitoring' actor system
  val system = ActorSystem("monitoring")

  val athena = system.actorOf(Props[Athena], "athena")

  val ares = system.actorOf(Props(classOf[Ares], athena), "ares")

  athena ! "Hi"

  Thread.sleep(500)

  system.terminate()
}
