import org.json4s.native.JsonMethods._

import scala.util.Try
import Util._

object Twitter {

  def parseTweet(json : String) : Try[Tweet] = {
    Try(parse(json).extract[Tweet])
  }

  def processTweet(tweet : Tweet): Unit = {
    totalCount(tweet)
  }

  def totalCount(tweet : Tweet): Unit = {
    val count =  map.getOrElse("count",0)
    map("count") = count + 1
  }
}
