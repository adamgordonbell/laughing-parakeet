package  com.cascadeofinsights.twitterstreat.analytics

import com.cascadeofinsights.twitterstreat.Tweet
import org.joda.time.{DateTime, Duration}

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
     val (secondCount, secondTotal) = data.filterKeys(_.startsWith(key)).foldRight((0,0))((kv,count) => (count._1 +1, count._2 + kv._2))
     if (secondTotal == 0){
       0.toDouble
     } else {
       secondTotal.toDouble / secondCount
     }
   }

  def result(): (Double, Double, Double) =  (getAverageByKey("SEC:"),getAverageByKey("MIN:"),getAverageByKey("HOUR:"))
}
