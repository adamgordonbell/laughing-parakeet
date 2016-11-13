package com.cascadeofinsights.twitterstream.analytics

import com.cascadeofinsights.twitterstream.Tweet
import com.cascadeofinsights.twitterstream.Util._

object Total extends  CountAnalytics[Int] {

  def process(t: Tweet): Unit = {
    data.IncrementByKey("total")
  }

  override def result(): Int = data.getCount("total")
}
