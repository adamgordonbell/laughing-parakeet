package com.cascadeofinsights.twitterstream

import akka.stream.scaladsl.{Broadcast, Flow, Sink, Source}
import Util._
import akka.{Done, NotUsed}
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
      source.buffer(1000, OverflowStrategy.dropTail) ~>
      bcast ~> Flow[Tweet].map(TotalFlow.process(_))              ~> new PersistSink[Int]("total").sink
      bcast ~> Flow[Tweet].map(AveragesFlow.process(_))           ~> new PersistSink[(Stats, Stats, Stats)]("ave").sink
      bcast ~> Flow[Tweet].map(CountPhotosAndUrlsFlow.process(_)) ~> new PersistSink[(Int, Int)]("urls").sink
      bcast ~> Flow[Tweet].map(TopHashtagsFlow.process(_))        ~> new PersistSink[List[(String, Int)]]("hash").sink
      bcast ~> Flow[Tweet].map(TopDomainsFlow.process(_))         ~> new PersistSink[List[(String, Int)]]("domains").sink
      bcast ~> Flow[Tweet].map(TopEmojiFlow.process(_))           ~> new PersistSink[List[(String, Int)]]("emoji").sink
      ClosedShape
    })
  }

  def print(x : Option[Int]) : Unit = {
    x.map(println(_))
  }

  def printAnalyticsToConsole(): Unit = {
    val total = Config.DB.getOrElse("total",0)
    println(s"Count: ${total}")

    Config.DB.get("ave").asInstanceOf[Option[(Stats,Stats,Stats)]].map{ case (seconds, minutes, hours) =>
      println(s"Seconds $seconds")
      println(s"Minutes $minutes")
      println(s"Hours $hours")
    }

    Config.DB.get("urls").asInstanceOf[Option[(Int,Int)]].map { case (photo_Count, url_Count) =>
      println(s"Percent With URL: ${(photo_Count.toDouble / TotalFlow.result).asPercentage}")
      println(s"Percent With Photos: ${(url_Count.toDouble / TotalFlow.result).asPercentage}")
    }

    println(s"Top Hashtags:")
    Config.DB.get("hash").asInstanceOf[Option[List[(String,Int)]]].map { items =>
      for ((h, c) <- items) {
              println(s"\t$h")
      }
    }

    println(s"Top Domains:")
    Config.DB.get("domains").asInstanceOf[Option[List[(String,Int)]]].map { items =>
      for ((h, c) <- items) {
        println(s"\t$h")
      }
    }
    println(s"Top Emojis:")
    Config.DB.get("emoji").asInstanceOf[Option[List[(String,Int)]]].map { items =>
      for ((h, c) <- items) {
        println(s"\t$h")
      }
    }
  }
}