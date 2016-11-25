package com.xebia
package exercise3

import akka.actor.{Actor, Props}
import com.xebia.exercise3.ReverserFactory.AsyncReverseFunction

import scala.concurrent.ExecutionContext
import scala.util.Success

object ReverseActor {
  def props = Props[ReverseActor]

  def name = "reverser"

  sealed trait Result

  case class Reverse(value: String)

  case class ReverseResult(value: String) extends Result

  case object PalindromeResult extends Result

  case class Init()

  case class NotInitialized() extends Result

  //DONE_TODO add Init message which the ReverseActor sends to itself once
  // the ReverseFunction is available.

  //DONE_TODO add NotInitialized message to indicate the ReverseActor is not ready yet,
  // which extends Result trait like the other Result messages
}

class ReverseActor extends Actor {

  import ReverseActor._

  implicit val executionContext: ExecutionContext = context.dispatcher.prepare()

  //TODO_DONE send Init to self (right here or override preStart)
  override def preStart() = {
    self ! Init
  }

  // DONE_TODO the receive method should be set to the uninitialized Receive function

  def receive: Receive = uninitialized

  // DONE_TODO create an uninitialized Receive method
  // - Send back NotInitialized on a Reverse message.
  // - Load the AsyncReverseFunction using the ReverserFactory on receiving Init.
  // - call become to transition to initialized state once the Future completes successfully.
  // - pass through the AsyncReverseFunction to the initialized Receive function instead of using a var.

  def uninitialized: Receive = {
    case Init => {
      ReverserFactory.loadReverser.onComplete {
        case Success(reverse) => {
          context.become(initialized(reverse))
        }
      }
    }
    case Reverse(_) => {
      sender ! NotInitialized()
    }
  }

  def createResult(str: String, value: String)={
    if (str.equals(value)) PalindromeResult
    else ReverseResult(value)
  }

  //DONE_TODO Implement the initialized Receive function:
  // call the AsyncReverseFunction,
  // on completion of the Future send back to a **captured sender**
  // (do not close over sender but create a val which contains the sender at the time of receiving the Reverse message).
  def initialized(reverse: AsyncReverseFunction): Receive = {
    case Reverse(str) => {
      val sendTo = sender

      reverse(str).map(value => sendTo ! createResult(str,value))
    }
  }

}
