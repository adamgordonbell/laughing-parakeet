
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
import scala.util.Try
import Util._

object StreamConsumer extends App {


  val oauthHeader: Future[String] = oAuthRequest.map(_.header)

  val stream: Future[Source[Tweet, Any]] = oauthHeader.flatMap(requestStreamWithAuth)
             .map(toStreamSource)
  stream.map(_.runForeach(Twitter.processTweet))

  println("Server Streaming\nPress RETURN for stats\n Type quit to exit")
  while(StdIn.readLine() != "quit") {
    println(s"Count: ${map.getOrElse("total",0)}")
    val (secondCount, secondTotal) = map.filterKeys(_.startsWith("SEC:")).foldRight((0,0))((kv,count) => (count._1 +1, count._2 + kv._2))
    println(s"Average Per Second: $secondTotal / $secondCount")
  }
  system.terminate()


}