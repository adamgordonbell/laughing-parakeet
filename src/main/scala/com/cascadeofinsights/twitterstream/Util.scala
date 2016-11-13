package com.cascadeofinsights.twitterstream

import java.text.NumberFormat

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.hunorkovacs.koauth.domain.KoauthRequest
import com.hunorkovacs.koauth.service.consumer.{DefaultConsumerService, RequestWithInfo}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.util.Try

object Util {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val formats = DefaultFormats


  val oAuthRequest: Future[RequestWithInfo] = {
    val consumer = new DefaultConsumerService(system.dispatcher)
    consumer.createOauthenticatedRequest(
      KoauthRequest(
        method = "GET",
        url = Config.url,
        authorizationHeader = None,
        body = None
      ),
      Config.consumerKey,
      Config.consumerSecret,
      Config.accessToken,
      Config.accessTokenSecret
    )
  }

  def requestStreamWithAuth(header : String): Future[HttpResponse] = {
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

    val httpRequest: HttpRequest = HttpRequest(
      method = HttpMethods.GET,
      uri = Uri(Config.url),
      headers = httpHeaders
    )

    Http().singleRequest(httpRequest)
  }

  def toStreamSource(response : HttpResponse) : Source[Tweet,Any] = {
    def parseTweet(json : String) : Option[Tweet] = {
      Try(parse(json).extract[Tweet]).toOption
    }
    if (response.status.intValue() != 200) {
      println(response.entity.dataBytes.runForeach(_.utf8String))
      Source.empty
    } else {
      response.entity.withoutSizeLimit().dataBytes
        .scan("")((acc, curr) => if (acc.contains("\r\n")) curr.utf8String else acc + curr.utf8String)
        .filter(_.contains("\r\n"))
        .map(parseTweet)
        .map(_.toList)
        .mapConcat(identity)
    }
  }

  implicit class DoubleAsPercentage(d: Double) {
    def asPercentage = NumberFormat.getPercentInstance.format(d)
  }

  implicit class TrieHelper(data : TrieMap[String,Int]){
    def IncrementByKey(key : String): Unit = {
      val count = data.getOrElse(key, 0)
      data(key) = count + 1
    }

    def getCount(key : String) : Int = {
      data.getOrElse(key,0)
    }

    def takeTopSorted(i : Int ): List[(String, Int)] = {
      data.toList.sortWith((a, b) => a._2 > b._2).take(i)
    }
  }
}