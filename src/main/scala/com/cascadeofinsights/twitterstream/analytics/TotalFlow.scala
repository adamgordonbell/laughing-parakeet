package com.cascadeofinsights.twitterstream.analytics

import com.cascadeofinsights.twitterstream.Tweet
import com.cascadeofinsights.twitterstream.Util._

object TotalFlow extends CountAnalyticsFlow[Int] {

  def process(t: Tweet): Some[Int] = {
    data.IncrementByKey("total")
    Some(result())
  }

  def result(): Int = data.getCount("total")
}
