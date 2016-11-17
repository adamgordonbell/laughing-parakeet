package com.cascadeofinsights.twitterstream.analytics

import akka.Done
import akka.stream.scaladsl.Sink
import com.cascadeofinsights.twitterstream.{Config, Tweet}
import com.cascadeofinsights.twitterstream.Util._

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

trait AnalyticsFlow[T]  {
  def process(t : Tweet) : Option[T]
}

trait CountAnalyticsFlow[T] extends AnalyticsFlow[T] {
  protected val data : TrieMap[String,Int] = TrieMap[String,Int]()
  private val persistCount = 10
  private var counter = persistCount

  def result() : T

  def optionResult() = {
    counter -= 1
    if(counter < 1){
      counter = persistCount
      Some(result)
    } else {
      None
    }
  }

}
trait TopAnalyticsFlow[T] extends AnalyticsFlow[T] {
  private val maxSize = 1000
  private val shrinkSize = 500
  private val persistCount = 10

  protected var data : TrieMap[String,Int] = TrieMap[String,Int]()
  private var counter = persistCount

  def result() : T

  def optionResult(): Option[T] = {
    counter -= 1
    if(counter < 1){
      counter = persistCount
      Some(result)
    } else {
      None
    }
  }
  /*
    Limit Size of data, by only keeping the top shrinkSize counts when above maxSize

    Renormalize counts towards 1:
    To prevent bias against dropped values, the lowest value is subtracted from all keys. This gives the lowest key a
    value of 1. This is like assuming that all keys not being tracked have the value of one less than smallest key
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

class PersistSink[T](key : String){
  def sink : Sink[Option[T],Future[Done]] = {
    Sink.foreach(t => persistIfDefined(t))
  }

  def persistIfDefined(x : Option[T]) : Unit = {
    x.map { value =>
//      println(key)
      Config.DB(key) = value.asInstanceOf[AnyVal]
    }
  }
}
