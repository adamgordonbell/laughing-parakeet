package  com.cascadeofinsights.twitterstreat.analytics

import com.cascadeofinsights.twitterstreat.Tweet

object Total extends  Analytics[Int] {

  def process(t: Tweet): Unit = {
    IncrementByKey("total")
  }

  override def result(): Int = data.getOrElse("total",0)
}
