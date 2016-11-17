package com.cascadeofinsights.twitterstream.analytics

import com.cascadeofinsights.twitterstream.Tweet
import com.cascadeofinsights.twitterstream.Util._

object CountPhotosAndUrlsFlow extends  CountAnalyticsFlow[(Int,Int)] {

  def process(t: Tweet): Option[(Int, Int)] = {
    if(!t.entities.media.isEmpty) {
      data.IncrementByKey("photoCount")
    }
    if(!t.entities.urls.isEmpty) {
      data.IncrementByKey("urlCount")
    }
    optionResult()
  }

  def result(): (Int, Int) = (data.getCount("photoCount"),data.getCount("urlCount"))
}
