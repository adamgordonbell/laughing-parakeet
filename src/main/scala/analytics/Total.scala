package  com.cascadeofinsights.twitterstreat.analytics

import com.cascadeofinsights.twitterstreat.Tweet
import com.cascadeofinsights.twitterstreat.Util._

object Total extends  CountAnalytics[Int] {

  def process(t: Tweet): Unit = {
    data.IncrementByKey("total")
  }

  override def result(): Int = data.getCount("total")
}
