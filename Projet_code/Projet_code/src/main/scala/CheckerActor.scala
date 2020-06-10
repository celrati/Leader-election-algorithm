package upmc.akka.leader

import java.util
import java.util.Date

import akka.actor._


import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

abstract class Tick
case class CheckerTick () extends Tick

class CheckerActor (val id:Int, val terminaux:List[Terminal], electionActor:ActorRef) extends Actor {

  var time : Int = 500
  val father = context.parent

  var nodesAlive:List[Int] = List()
  var datesForChecking:List[Date] = List()
  var lastDate:Date = null

  var leader : Int = -2
  var lastSize : Int = -1

	def qsort(list: List[Int]): List[Int] = {
			list match {
			case Nil        => Nil
			case a :: Nil   => List(a)
			case a :: tail  => qsort(tail.filter(x=> x <= a)) ::: List(a) ::: qsort(tail.filter(x => x > a))
			}
		}

  def receive = {


    case Start => {

      Thread.sleep(time)
      self ! CheckerTick
      nodesAlive = id::nodesAlive
    }


    case IsAlive (nodeId) =>{
      if(!nodesAlive.contains(nodeId)){
        nodesAlive = nodeId::nodesAlive
      }
    }


    case IsAliveLeader (nodeId) => {

      if(!nodesAlive.contains(nodeId)){
        nodesAlive = nodeId::nodesAlive
      }
      leader = nodeId
    }

    case CheckerTick =>
    {
      if(nodesAlive.size >= 2)
      {
        if(!nodesAlive.contains(leader) && leader != -1)
        {
          father ! LeaderChanged(-1)
          leader = -1
          electionActor ! StartWithNodeList(nodesAlive)
        }
      }

      else if(lastSize != -1 && leader != id && leader != -1)
      {
        leader = -1
        electionActor ! StartWithNodeList(nodesAlive)
      }

      if(lastSize!=nodesAlive.size) nodesAlive = qsort(nodesAlive)

      lastSize = nodesAlive.size
      nodesAlive = id:: List()
      Thread.sleep(time)
      self ! CheckerTick

    }

  }

}