package com.cascadeofinsights.twitterstream.analytics

import com.cascadeofinsights.twitterstream.Tweet
import com.cascadeofinsights.twitterstream.Util._


object TopHashtagsFlow extends TopAnalyticsFlow[List[(String,Int)]] {

  def process(t: Tweet): Option[List[(String, Int)]] = {
    for (h <- t.entities.hashtags) {
      data.IncrementByKey(h.text)
    }
    resize()
    optionResult()
  }

  def result(): List[(String, Int)] = {
    data.takeTopSorted(10)
  }
}
