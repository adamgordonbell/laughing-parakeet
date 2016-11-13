package  com.cascadeofinsights.twitterstreat.analytics

import com.cascadeofinsights.twitterstreat.Util._
import com.cascadeofinsights.twitterstreat.Tweet


object TopDomains extends  TopAnalytics[List[(String,Int)]] {

  def process(t: Tweet): Unit = {
    for (h <- t.entities.urls) {
      data.IncrementByKey(h.url)
    }
    resize()
  }

  def result(): List[(String, Int)] = {
    data.takeTopSorted(10)
  }
}
