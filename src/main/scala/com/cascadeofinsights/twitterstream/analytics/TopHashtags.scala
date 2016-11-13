package com.cascadeofinsights.twitterstream.analytics

import com.cascadeofinsights.twitterstream.Tweet
import com.cascadeofinsights.twitterstream.Util._


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
