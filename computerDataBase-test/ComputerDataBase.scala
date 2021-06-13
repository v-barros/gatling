
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.util.Random

class ComputerDataBase extends Simulation {

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
	
	val numOfUsers : Int = 4		
	
	//val feeder = Iterator.continually(Map("pageNum" -> (Random.alphanumeric.take(20).mkString + "@foo.com")))
	val randPageNumFeeder = Iterator.continually(Map("pageNum" -> (Random.nextInt(10))))
	val searchFeeder = csv("computerDataBase-test-data/search.csv").random

	object HomePage{
		val name = "homePage"
		val homePage = 	exec(http(name)
							.get("/computers"))
						.pause(pauseTime)
	}
	object ProductPage{
		val name = "productPage_"
		val productPage = 	exec{(
								http(name+"${computerURL}")
								.get("${computerURL}"))}
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
								feed(randPageNumFeeder)
								.exec(http(name+"${pageNum}")
									.get("/computers?p=${pageNum}&n=10&s=name&d=asc"))
								.pause(pauseTime)
							}			
	}
	object ProductSearch{
		val name = "productSearch_"
		val productSearch = feed(searchFeeder)
							.exec(http(name+"${searchCriterion}")
								.get("/computers?f=${searchCriterion}") //searchCriterion is the name of a Column in "search.csv"
								.check(css("a:contains('${searchComputerName}')", "href").saveAs("computerURL")))
	}
	val scn = scenario("HomePageAndPageNav")
				.exec(HomePage.homePage,
					PageNavigation.pageNavigation)
	val scn2 = scenario("FullScenario")
				.exec(HomePage.homePage,
					PageNavigation.pageNavigation,
					ProductSearch.productSearch,
					ProductPage.productPage,
					ChangeProduct.changeProduct)
				
	setUp(scn.inject(constantConcurrentUsers(10).during(10.seconds)),
		scn2.inject(constantUsersPerSec(1).during(10.seconds)))
	.protocols(httpProtocol)
}