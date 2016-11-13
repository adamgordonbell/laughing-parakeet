package  com.cascadeofinsights.twitterstreat.analytics

import com.cascadeofinsights.twitterstreat.Tweet
import com.cascadeofinsights.twitterstreat.Util._
import org.joda.time.{DateTime, Duration}

object Averages extends  CountAnalytics[(Double,Double,Double)] {

  val midnight = new DateTime().withTimeAtStartOfDay()

  def process(t: Tweet): Unit = {
    t.timestamp match {
      case Some(d) =>
        val duration = new Duration(midnight, d)
        data.IncrementByKey("SEC:"+duration.toStandardSeconds().getSeconds())
        data.IncrementByKey("MIN:"+duration.toStandardMinutes.getMinutes())
        data.IncrementByKey("HOUR:"+duration.toStandardHours.getHours())
      case None =>
    }
  }
   private def getAverageByKey(key : String) : Double = {
     val (count, total) = data.filterKeys(_.startsWith(key))
                            .foldRight((0,0))((kv,count) => (count._1 +1, count._2 + kv._2))
     if (total == 0){
       0.toDouble
     } else {
       total.toDouble / count
     }
   }

  def result(): (Double, Double, Double) = {
    (getAverageByKey("SEC:")
    ,getAverageByKey("MIN:")
    ,getAverageByKey("HOUR:"))
  }
}
