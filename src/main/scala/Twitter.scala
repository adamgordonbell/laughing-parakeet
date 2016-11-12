

import scala.util.Try
import Util._
import org.joda.time.{DateTime, Duration}

object Twitter {


  def processTweet(tweet : Tweet): Unit = {
    totalCount(tweet)
    averageCount(tweet)
  }

  def totalCount(tweet : Tweet): Unit = {
    IncrementByKey("total")
  }

  val midnight = new DateTime().withTimeAtStartOfDay()
  def averageCount(tweet : Tweet): Unit = {
    tweet.timestamp match {
      case Some(d) =>
        val duration = new Duration(midnight, d)
        IncrementByKey("SEC:"+duration.toStandardSeconds().getSeconds())
        IncrementByKey("MIN:"+duration.toStandardMinutes.getMinutes())
        IncrementByKey("HOUR:"+duration.toStandardHours.getHours())
      case None =>
    }
  }

  def IncrementByKey(key : String): Unit = {
    val count = map.getOrElse(key, 0)
    map(key) = count + 1
  }

}
