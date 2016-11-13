package  com.cascadeofinsights.twitterstreat.analytics

import scala.collection.concurrent.TrieMap
import com.cascadeofinsights.twitterstreat.Tweet
import com.cascadeofinsights.twitterstreat.Util._

trait Analytics[T] {
  val data : TrieMap[String,Int] = map
  def process(t : Tweet) : Unit
  def result() : T

  def IncrementByKey(key : String): Unit = {
    val count = data.getOrElse(key, 0)
    data(key) = count + 1
  }
}
