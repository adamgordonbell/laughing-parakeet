package com.cascadeofinsights.twitterstream.analytics

import java.text.DecimalFormat

import com.cascadeofinsights.twitterstream.Tweet
import com.cascadeofinsights.twitterstream.Util._
import org.joda.time.{DateTime, Duration}

object AveragesFlow extends  CountAnalyticsFlow[(Stats, Stats, Stats)] {

  val midnight = new DateTime().withTimeAtStartOfDay()

  def process(t: Tweet): Option[(Stats, Stats, Stats)]  = {
    t.timestamp match {
      case Some(d) =>
        val duration = new Duration(midnight, d)
        data.IncrementByKey("SEC:"+duration.toStandardSeconds().getSeconds())
        data.IncrementByKey("MIN:"+duration.toStandardMinutes.getMinutes())
        data.IncrementByKey("HOUR:"+duration.toStandardHours.getHours())
      case None =>
    }
    optionResult()
  }

   private def getAverageByKey(key : String) : Double = {
     val (count, total) = mapByKey(key)
                            .foldRight((0,0))((kv,count) => (count._1 +1, count._2 + kv._2))
     if (total == 0){
       0.toDouble
     } else {
       total.toDouble / count
     }
   }

  private def getMaxByKey(key : String) : Int = {
     mapByKey(key)
      .foldRight(0)((kv,max) => Math.max(max,kv._2))
  }

  private def getMinByKey(key : String) : Int = {
    mapByKey(key)
      .foldRight(Int.MaxValue)((kv,max) => Math.min(max,kv._2))
  }

  private def mapByKey(key: String) = {
    data.filterKeys(_.startsWith(key))
  }

  private def statsByKey(key : String) = {
    Stats(getAverageByKey(key),getMinByKey(key),getMaxByKey(key))
  }

  def result(): (Stats, Stats, Stats) = {
    (statsByKey("SEC:")
    ,statsByKey("MIN:")
    ,statsByKey("HOUR:"))
  }
}


case class Stats(average : Double, min : Int, max : Int){
  override def toString: String = {
    val ave = new DecimalFormat("##.##").format(average)
    s"Average / Min / Max : $ave $min $max"
  }
}