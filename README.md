# Twitter Stream Analytics
The Twitter Streaming API provides real-time access to public tweets. In this assignment you will build an application that connects to the Streaming API and processes incoming tweets to compute various statistics. Feel free to use any programming language and frameworks/libraries you want to accomplish this task.
The sample endpoint provides a random sample of approximately 1% of the full tweet stream. Your app should consume this sample stream and keep track of the following:
* Total number of tweets received
* Average tweets per hour/minute/second
* Top emojis in tweets
* Percent of tweets that contains emojis
* Top hashtags
* Percent of tweets that contain a url
* Percent of tweets that contain a photo url (pic.twitter.com or instagram)
* Top domains of urls in tweets

## Usage:

```sbt run ```

## Example Run:
```
Running com.cascadeofinsights.twitterstream.TwitterStreamAnalyticsApp 
Receiving Twitter Stream
Press RETURN for stats
 Type quit to exit

Count: 31461
Seconds Average / Min / Max : 40.03 1 75
Minutes Average / Min / Max : 2247.21 616 2847
Hours Average / Min / Max : 15730.5 12053 19408
Percent With URL: 29%
Percent With Photos: 24%
Top Hashtags:
	ImACeleb
	MTVStars5SOS
	真田丸
	Радио
	AMAs
	NFL
	MTVStarsLadyGaga
	Архив_радио
	sunset
	Онлайн_радио
Top Domains:
	https://t.co/M3Cg9ledou
	https://t.co/M9vzEusNOl
	https://t.co/6Wwb80ebpj
	https://t.co/X3lmn0ivwE
	https://t.co/xliHaxkClx
	https://t.co/UMCsoD4yYs
	https://t.co/ewKrlOoZe8
	https://t.co/QaGwgHHevL
	https://t.co/LiGLi5ODaJ
	https://t.co/wsB7ptHJNv
Top Emojis:
	😂 (:joy:)
	😍 (:heart_eyes:)
	❤ (:heart:)
	😳 (:flushed:)
	♥ (:hearts:)
	💀 (:skull:)
	🙏 (:pray:)
	💕 (:two_hearts:)
	🔥 (:fire:)
	👊 (:facepunch:)
quit
```