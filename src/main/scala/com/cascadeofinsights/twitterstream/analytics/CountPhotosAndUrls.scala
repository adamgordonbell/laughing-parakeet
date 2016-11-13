package com.cascadeofinsights.twitterstream.analytics

import com.cascadeofinsights.twitterstream.Tweet
import com.cascadeofinsights.twitterstream.Util._

object CountPhotosAndUrls extends  CountAnalytics[(Int,Int)] {

  def process(t: Tweet): Unit = {
    if(!t.entities.media.isEmpty) {
      data.IncrementByKey("photoCount")
    }
    if(!t.entities.urls.isEmpty) {
      data.IncrementByKey("urlCount")
    }
  }

  def result(): (Int, Int) = (data.getCount("photoCount"),data.getCount("urlCount"))
}
