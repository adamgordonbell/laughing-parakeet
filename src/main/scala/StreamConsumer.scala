
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
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

  oauthHeader.flatMap(requestStreamWithAuth)
             .flatMap(x => toStreamSource(x)
               .map(Twitter.parseTweet)
               .runForeach(_.map(Twitter.processTweet)))

  println("Server Streaming\nPress RETURN for stats\n Type quit to exit")
  while(StdIn.readLine() != "quit") {
    println(s"Count: ${map("count")}")
  }
  system.terminate()



}