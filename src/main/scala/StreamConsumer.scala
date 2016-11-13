package  com.cascadeofinsights.twitterstreat

import akka.stream.scaladsl.Source
import com.cascadeofinsights.twitterstreat.Util._
import analytics.{Averages, CountPhotosAndUrls, TopHashtags, Total}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn
import scala.util.{Failure, Success}

object StreamConsumer extends App {


  val oauthHeader: Future[String] = oAuthRequest.map(_.header)


  val stream: Future[Source[Tweet, Any]] = oauthHeader.flatMap(requestStreamWithAuth)
             .map(toStreamSource)
  stream.flatMap(_.runForeach { t =>
    Total.process(t)
    Averages.process(t)
    CountPhotosAndUrls.process(t)
    TopHashtags.process(t)
  }).onComplete {
    case Success(d) => println("Stream ended without error")
    case Failure(t) => println("An error has occured: " + t.getMessage)
  }

  println("Server Streaming\nPress RETURN for stats\n Type quit to exit")
  while(StdIn.readLine() != "quit") {

    println(s"Count: ${Total.result()}")
    val (seconds, minutes, hours) = Averages.result()

    println(s"Average Per Second: $seconds")
    println(s"Average Per Minute: $minutes")
    println(s"Average Per Hour: $hours")

    val (photo_Count,url_Count) = CountPhotosAndUrls.result
    println(s"Percent With URL: ${(photo_Count.toDouble / Total.result).asPercentage}")
    println(s"Percent With Photos: ${(url_Count.toDouble / Total.result).asPercentage}")

    println(s"Top Hashtags:")
   for( (h,c) <- TopHashtags.result()){
      println(s"\t$h ($c)")
   }
    println(s"size : ${TopHashtags.data.size}")
  }
  system.terminate()


}