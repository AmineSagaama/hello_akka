package com.asagaama.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.asagaama.actor.Checker.{BlackUser, CheckUser, WhiteUser}
import com.asagaama.actor.Recorder.NewUser
import com.asagaama.actor.Storage.AddUser

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by asagaama on 05/06/2017.
  */

case class User(username: String, email: String)


object Recorder {

  def props(checker: ActorRef, storage: ActorRef) =
    Props(new Recorder(checker, storage))

  sealed trait RecorderMsg

  // Recorder Messages
  case class NewUser(user: User) extends RecorderMsg

}

object Checker {

  sealed trait CheckerMsg

  sealed trait CheckerResponse

  // Checker Messages
  case class CheckUser(user: User) extends CheckerMsg

  // Checker Responses
  case class BlackUser(user: User) extends CheckerResponse

  case class WhiteUser(user: User) extends CheckerResponse

}

object Storage {

  sealed trait StorageMsg

  // Storage Messages
  case class AddUser(user: User) extends StorageMsg

}

class Storage extends Actor {
  var users = List.empty[User]

  def receive = {
    case AddUser(user) => println(s"Storage: $user added")
      users = user :: users
  }
}

class Checker extends Actor {
  val blackList = List(User("test", "test@test.com"))

  def receive = {
    case CheckUser(user) if blackList.contains(user) =>
      println(s"Checker: $user in the blacklist")
      sender() ! BlackUser(user)
    case CheckUser(user) =>
      println(s"Checker: $user not in the blacklist")
      sender() ! WhiteUser(user)
  }
}

class Recorder(checker: ActorRef, storage: ActorRef) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout(5 seconds)

  def receive = {
    case NewUser(user) =>
      checker ? CheckUser(user) map {
        case WhiteUser(user) =>
          storage ! AddUser(user)
        case BlackUser(user) =>
          println(s"Recorder: $user in the blacklist")
      }
  }
}

object TalkToActor extends App {

  // Create the 'talk-to-actor' actor system
  val system = ActorSystem("talk-to-actor")

  // Create the 'checker' actor
  val checker = system.actorOf(Props[Checker], "checker")

  // Create the 'storage' actor
  val storage = system.actorOf(Props[Storage], "storage")

  // Create the 'recorder' actor
  val recorder = system.actorOf(Recorder.props(checker, storage), "recorder")

  // send NewUser Message to recorder
  recorder ! Recorder.NewUser(User("amine", "amine@test.com"))

  Thread.sleep(100)

  // shutdown system
  system.terminate()


}
