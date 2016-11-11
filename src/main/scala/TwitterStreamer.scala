/**
  * This is a fully working example of Twitter's Streaming API client.
  * NOTE: this may stop working if at any point Twitter does some breaking changes to this API or the JSON structure.
  */

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.hunorkovacs.koauth.domain.KoauthRequest
import com.hunorkovacs.koauth.service.consumer.DefaultConsumerService
import com.typesafe.config.ConfigFactory
import org.json4s._
import org.json4s.native.JsonMethods._

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn
import scala.util.Try

object TwitterStreamer extends App {

  val map = TrieMap[String,Int]()
  val conf = ConfigFactory.load()

  private val consumerKey = "ihircCxcTQKrirwUJOBvu5z6C"
  private val consumerSecret = "jVLlHEEuDRcaHP0LVmmwxVs7KZQseUX2Ca0uGAXe0sMO2wEI76"
  private val accessToken = "191034488-PL77OgNpRiYgAS5rxJkB77NFwqXbK7NELmt5Fw2k"
  private val accessTokenSecret = "TSPVOV9M5i6KPECmsqWQIOeGiOWzIjGCDWBlz0piFJaLv"
  private val url = "https://stream.twitter.com/1.1/statuses/sample.json"

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val formats = DefaultFormats

  private val consumer = new DefaultConsumerService(system.dispatcher)

  //Create Oauth 1a header
  val oauthHeader: Future[String] = consumer.createOauthenticatedRequest(
    KoauthRequest(
      method = "GET",
      url = url,
      authorizationHeader = None,
      body = None
    ),
    consumerKey,
    consumerSecret,
    accessToken,
    accessTokenSecret
  ).map(_.header)

  oauthHeader.flatMap(getResponse)
             .flatMap(x => processResponse(x)
               .map(parseTweet)
               .runForeach(_.map(processTweet)))

  println("Server Streaming\nPress RETURN for stats\n Type quit to exit")
  while(StdIn.readLine() != "quit") {
    println(s"Count: ${map("count")}")
  }
  system.terminate()

  def getResponse(header : String): Future[HttpResponse] = {
      val httpHeaders: List[HttpHeader] = List(
        HttpHeader.parse("Authorization", header) match {
          case ParsingResult.Ok(h, _) => Some(h)
          case _ => None
        },
        HttpHeader.parse("Accept", "*/*") match {
          case ParsingResult.Ok(h, _) => Some(h)
          case _ => None
        }
      ).flatten

      println("got headers")
      println(httpHeaders)
      val httpRequest: HttpRequest = HttpRequest(
        method = HttpMethods.GET,
        uri = Uri(url),
        headers = httpHeaders
      )

      Http().singleRequest(httpRequest)
  }

  def processResponse(response : HttpResponse) : Source[String,Any] = {
      if (response.status.intValue() != 200) {
        println(response.entity.dataBytes.runForeach(_.utf8String))
        Source.empty
      } else {
        response.entity.dataBytes
          .scan("")((acc, curr) => if (acc.contains("\r\n")) curr.utf8String else acc + curr.utf8String)
          .filter(_.contains("\r\n"))
      }
  }

  def parseTweet(json : String) : Try[Tweet] = {
    Try(parse(json).extract[Tweet])
  }

  def processTweet(tweet : Tweet): Unit =
  {
   val count =  map.getOrElse("count",0)
    map("count") = count + 1
//    val v = map
//    map("count") = v + 1
//      println("-----")
//      println(tweet)
  }


}