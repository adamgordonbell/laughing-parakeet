
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.hunorkovacs.koauth.domain.KoauthRequest
import com.hunorkovacs.koauth.service.consumer.DefaultConsumerService
import org.json4s._
import org.json4s.native.JsonMethods._

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn
import scala.util.{Failure, Success, Try}
import Util._

object StreamConsumer extends App {


  val oauthHeader: Future[String] = oAuthRequest.map(_.header)


  val stream: Future[Source[Tweet, Any]] = oauthHeader.flatMap(requestStreamWithAuth)
             .map(toStreamSource)
  stream.flatMap(_.runForeach(Twitter.processTweet)).onComplete {
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