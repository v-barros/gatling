
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.util.Random

class RecordedSimulation2 extends Simulation {

	val httpProtocol = http
		.baseUrl("https://computer-database.gatling.io")
		.inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*detectportal\.firefox\.com.*"""), WhiteList())
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.5")
		.upgradeInsecureRequestsHeader("1")
		.userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:89.0) Gecko/20100101 Firefox/89.0")

	val headers_0 = Map("Cache-Control" -> "max-age=0")

	val headers_2 = Map("Origin" -> "https://computer-database.gatling.io")

	val pauseTime : Int = 1
	
	val numOfUsers : Int = 2
	
	//val feeder = Iterator.continually(Map("pageNum" -> (Random.alphanumeric.take(20).mkString + "@foo.com")))
	val feeder = Iterator.continually(Map("pageNum" -> (Random.nextInt(10))))

	object HomePage{
		val name = "homePage"
		val homePage = 	exec(http(name)
							.get("/computers"))
						.pause(pauseTime)
	}
	object ProductPage{
		val name = "productPage"
		val productPage = 	exec(http(name)
								.get("/computers/381"))
						 	.pause(pauseTime)
	}
	object ChangeProduct{
		val name = "changeProduct"
		val changeProduct = exec(http(name)
								.post("/computers/381")
								.formParam("name", "ACEACE")
								.formParam("introduced", "2020-01-01")
								.formParam("discontinued", "2021-01-01")
								.formParam("company", "2"))
							.pause(pauseTime)
	}
	object PageNavigation{
		val name = "pageNavigation_"
		val pageNavigation = repeat(5,"i")
							{
							exec(http(name+"${pageNum}")
								.get("/computers?p=${pageNum}&n=10&s=name&d=asc"))
							.pause(pauseTime)
							}			
	}
	object ProductSearch{
		val name = "productSearch"
		val productSearch = exec(http(name)
								.get("/computers?f=apple"))
	}
	val scn = scenario("HomePageAndPageNav")
			.feed(feeder)
			.exec(HomePage.homePage,
				PageNavigation.pageNavigation,
				)
	val scn2 = scenario("FullScenario")
			.feed(feeder)
			.exec(HomePage.homePage,
				PageNavigation.pageNavigation,
				ProductSearch.productSearch,
				ProductPage.productPage,
				ChangeProduct.changeProduct
				)
				
	setUp(scn.inject(atOnceUsers(numOfUsers)),
		scn2.inject(atOnceUsers(numOfUsers))).protocols(httpProtocol)
}