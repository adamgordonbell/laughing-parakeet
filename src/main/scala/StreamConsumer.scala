package  com.cascadeofinsights.twitterstreat

import akka.stream.scaladsl.Source
import com.cascadeofinsights.twitterstreat.Util._
import com.cascadeofinsights.twitterstreat.analytics.{Averages, Total}

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
  }
  system.terminate()


}