package com.cascadeofinsights.twitterstream

import scala.collection.concurrent.TrieMap

object Config {
  val consumerKey = "ihircCxcTQKrirwUJOBvu5z6C"
  val consumerSecret = "jVLlHEEuDRcaHP0LVmmwxVs7KZQseUX2Ca0uGAXe0sMO2wEI76"
  val accessToken = "191034488-PL77OgNpRiYgAS5rxJkB77NFwqXbK7NELmt5Fw2k"
  val accessTokenSecret = "TSPVOV9M5i6KPECmsqWQIOeGiOWzIjGCDWBlz0piFJaLv"
  val url = "https://stream.twitter.com/1.1/statuses/sample.json"

  val DB : TrieMap[String,AnyVal] = TrieMap[String,AnyVal]()
}
