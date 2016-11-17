package com.cascadeofinsights.twitterstream.analytics

import com.cascadeofinsights.twitterstream.Tweet
import com.cascadeofinsights.twitterstream.Util._


object TopDomainsFlow extends  TopAnalyticsFlow[List[(String,Int)]] {

  def process(t: Tweet) = {
    for (h <- t.entities.urls) {
      data.IncrementByKey(h.url)
    }
    resize()
    optionResult()
  }

  def result(): List[(String, Int)] = {
    data.takeTopSorted(10)
  }
}
