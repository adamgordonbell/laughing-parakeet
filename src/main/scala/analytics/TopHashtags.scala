package  com.cascadeofinsights.twitterstreat.analytics

import com.cascadeofinsights.twitterstreat.Tweet
import com.cascadeofinsights.twitterstreat.Util._


object TopHashtags extends  TopAnalytics[List[(String,Int)]] {

  def process(t: Tweet): Unit = {
    for (h <- t.entities.hashtags) {
      data.IncrementByKey(h.text)
    }
    resize()
  }

  def result(): List[(String, Int)] = {
    data.takeTopSorted(10)
  }
}
