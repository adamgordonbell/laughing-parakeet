package  com.cascadeofinsights.twitterstreat.analytics

import scala.collection.concurrent.TrieMap
import com.cascadeofinsights.twitterstreat.Tweet
import com.cascadeofinsights.twitterstreat.Util._


trait TopAnalytics[T] extends Analytics[T] {
  val maxSize = 1000
  val shrinkSize = 500

  var data : TrieMap[String,Int] = TrieMap[String,Int]()

  /*
    Limit Size of data, by only keeping the top shrinkSize counts when above maxSize
    To prevent bias against dropped values, the lowest value is subtracted from all keys
    This is like assuming that all keys not being tracked have the value of one less than smallest key
   */
  def resize() = {
    if(data.size > maxSize){
      val tops = data.takeTopSorted(shrinkSize)
      val decrement = tops.last._2 -1
      val newValues = tops.map(x => (x._1,x._2-decrement))
      data = TrieMap(newValues: _*)
    }
  }
}

trait CountAnalytics[T] extends Analytics[T] {
  val data : TrieMap[String,Int] = TrieMap[String,Int]()
}

trait Analytics[T] {
  def process(t : Tweet) : Unit
  def result() : T
}
