package com.cascadeofinsights.twitterstream.analytics

import com.cascadeofinsights.twitterstream.Tweet
import com.cascadeofinsights.twitterstream.Util._
import com.vdurmont.emoji.EmojiParser

object TopEmoji extends  TopAnalytics[List[(String,Int)]] {

  // match :smile: for example
  val regex = "(?<=\\:)[a-z]+[a-z0-9_]+(?=\\:)".r

  def process(t: Tweet): Unit = {
    for(m <- regex.findAllIn(EmojiParser.parseToAliases(t.text))){
      data.IncrementByKey(m)
    }
    resize()
  }

  def result(): List[(String, Int)] = {
    data.takeTopSorted(10)
  }
}
