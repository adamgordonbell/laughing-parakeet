

import scala.util.Try
import Util._
import org.joda.time.{DateTime, Duration}

import scala.collection.concurrent.TrieMap

object Twitter {
  def processTweet(tweet : Tweet): Unit = {
    Total.process(tweet)
    Averages.process(tweet)
  }
}

object Averages extends  Analytics[(Double,Double,Double)] {

  val midnight = new DateTime().withTimeAtStartOfDay()

  def process(t: Tweet): Unit = {
    t.timestamp match {
      case Some(d) =>
        val duration = new Duration(midnight, d)
        IncrementByKey("SEC:"+duration.toStandardSeconds().getSeconds())
        IncrementByKey("MIN:"+duration.toStandardMinutes.getMinutes())
        IncrementByKey("HOUR:"+duration.toStandardHours.getHours())
      case None =>
    }
  }
   private def getAverageByKey(key : String) : Double = {
     val (secondCount, secondTotal) = map.filterKeys(_.startsWith(key)).foldRight((0,0))((kv,count) => (count._1 +1, count._2 + kv._2))
     if (secondTotal == 0){
       0.toDouble
     } else {
       secondTotal.toDouble / secondCount
     }
   }

  def result(): (Double, Double, Double) =  (getAverageByKey("SEC:"),getAverageByKey("MIN:"),getAverageByKey("HOUR:"))
}

object Total extends  Analytics[Int] {

  def process(t: Tweet): Unit = {
    IncrementByKey("total")
  }

  override def result(): Int = map.getOrElse("total",0)
}

trait Analytics[T] {
  val data : TrieMap[String,Int] = map
  def process(t : Tweet) : Unit
  def result() : T

  def IncrementByKey(key : String): Unit = {
    val count = map.getOrElse(key, 0)
    map(key) = count + 1
  }
}
