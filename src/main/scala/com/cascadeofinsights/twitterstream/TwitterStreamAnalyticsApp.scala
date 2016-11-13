package com.cascadeofinsights.twitterstream

import akka.stream.scaladsl.{Broadcast, Flow, Sink, Source}
import Util._
import akka.NotUsed
import akka.stream.{ClosedShape, OverflowStrategy}
import com.cascadeofinsights.twitterstream.analytics._
import com.vdurmont.emoji.EmojiManager

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn
import scala.util.{Failure, Success}
import akka.stream.scaladsl._

object TwitterStreamAnalyticsApp extends App {

  val oauthHeader: Future[String] = oAuthRequest.map(_.header)

  val stream: Future[Source[Tweet, Any]] = oauthHeader.flatMap(requestStreamWithAuth)
    .map(toStreamSource)

  stream.map(x => getGraph(x).run()).onComplete {
    case Success(d) => println("Stream flow started without error")
    case Failure(t) => println("An error has occured: " + t.getMessage)
  }

  println("Receiving Twitter Stream\nPress RETURN for stats\n Type quit to exit")
  while (StdIn.readLine() != "quit") {
    printAnalyticsToConsole()
  }
  system.terminate()

  /*
   * Given a source of Tweets, build a graph that broadcasts each tweet to each Analytics Sink
   */
  def getGraph(source: Source[Tweet, Any]): RunnableGraph[NotUsed] = {
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val bcast = builder.add(Broadcast[Tweet](6))
      source.buffer(1000, OverflowStrategy.dropTail) ~> bcast ~> Sink.foreach(Total.process)
      bcast ~> Sink.foreach(Averages.process)
      bcast ~> Sink.foreach(CountPhotosAndUrls.process)
      bcast ~> Sink.foreach(TopHashtags.process)
      bcast ~> Sink.foreach(TopDomains.process)
      bcast ~> Sink.foreach(TopEmoji.process)
      ClosedShape
    })
  }

  def printAnalyticsToConsole(): Unit = {
    println(s"Count: ${Total.result()}")
    val (seconds, minutes, hours) = Averages.result()

    println(s"Seconds $seconds")
    println(s"Minutes $minutes")
    println(s"Hours $hours")

    val (photo_Count, url_Count) = CountPhotosAndUrls.result
    println(s"Percent With URL: ${(photo_Count.toDouble / Total.result).asPercentage}")
    println(s"Percent With Photos: ${(url_Count.toDouble / Total.result).asPercentage}")

    println(s"Top Hashtags:")
    for ((h, c) <- TopHashtags.result()) {
      println(s"\t$h")
    }

    println(s"Top Domains:")
    for ((h, c) <- TopDomains.result()) {
      println(s"\t$h")
    }

    println(s"Top Emojis:")
    for ((h, c) <- TopEmoji.result()) {
      val unicode = EmojiManager.getForAlias(h).getUnicode()
      println(s"\t$unicode (:$h:)")
    }
  }
}