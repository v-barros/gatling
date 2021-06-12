
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class RecordedSimulation extends Simulation {

	val httpProtocol = http
		.baseUrl("https://computer-database.gatling.io")
		.inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*detectportal\.firefox\.com.*"""), WhiteList())
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.5")
		.upgradeInsecureRequestsHeader("1")
		.userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:89.0) Gecko/20100101 Firefox/89.0")

	val headers_0 = Map("Cache-Control" -> "max-age=0")

	val headers_5 = Map("Origin" -> "https://computer-database.gatling.io")



	val scn = scenario("RecordedSimulation")
		.exec(http("request_0")
			.get("/computers")
			.headers(headers_0))
		.pause(1)
		.exec(http("request_1")
			.get("/computers?p=1&n=10&s=name&d=asc"))
		.pause(1)
		.exec(http("request_2")
			.get("/computers?p=2&n=10&s=name&d=asc"))
		.pause(1)
		.exec(http("request_3")
			.get("/computers?f=Computer"))
		.pause(1)
		.exec(http("request_4")
			.get("/computers/567"))
		.pause(1)
		.exec(http("request_5")
			.post("/computers/567")
			.headers(headers_5)
			.formParam("name", "Dell Inspiron 560 Desktop Computer ")
			.formParam("introduced", "1981-11-23")
			.formParam("discontinued", "2019-11-23")
			.formParam("company", "11"))
		.pause(1)
		.exec(http("request_6")
			.get("/computers/new"))
		.pause(1)
		.exec(http("request_7")
			.post("/computers")
			.headers(headers_5)
			.formParam("name", "Apple 1")
			.formParam("introduced", "2000-07-28")
			.formParam("discontinued", "2020-01-02")
			.formParam("company", "1"))
		.exec(http("request_10")
			.get("/computers")
			.headers(headers_0))

	setUp(scn.inject(atOnceUsers(2))).protocols(httpProtocol)
}