package gatlingdemostore

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class DemostoreSimulation extends Simulation {
	
	/* Resources*/
	val dataFolder = "DemostoreSimulation-data"
	val categoryFeederCsv = "categories.csv"
	val productDetails = "productDetails.json"
	
	/* HttpRequest default*/
	val domain = "demostore.gatling.io"
	val httpProtocol = http
		.baseUrl("http://"+domain)
	
	/* Feeders*/
	val categoryFeeder = csv(dataFolder+"/"+categoryFeederCsv).random
	val jsonFeederProducts = jsonFile(dataFolder+"/"+productDetails).random
	
	
	val pauseTime : Int = 1

	
	object cmsPages{
		def homePage = {
			 exec(http("Home page")
				.get("/")
				.check(status.is(200))
				.check(regex("<title>Gatling Demo-Store</title>").exists)
				.check(css("#_csrf", "content").saveAs("csrf")))
		}
		def aboutUs = {
			exec(http("About Us")
				.get("/about-us")
				.check(status.is(200))
				.check(substring("About Us")))
		}
	}
	object Catalog{
		object Category {
			def view = {
				feed(categoryFeeder)
				.exec(http("List Products Page_${categoryName}")
					.get("/category/${categorySlug}")
					.check(status.is(200))
					.check(css("#CategoryName").is("${categoryName}")))
			}
		}
		object Product {
			def view = {
				feed(jsonFeederProducts)
				.exec(http("Load Product Page_${name}")
					.get("/product/${slug}")
					.check(status.is(200))
					.check(css("#ProductDescription").is("${description}")))
			}
			def add = {
				exec(view)
				.exec(http("Add Product to Cart")
					.get("/cart/add/${id}")
					.check(status.is(200))
					.check(substring("items in your cart")))
			}
		}
	}
	val scn = scenario("DemostoreSimulation")
		.exec(cmsPages.homePage)
		.pause(pauseTime)
		.exec(cmsPages.aboutUs)
		.pause(pauseTime)
		.exec(Catalog.Category.view)
		.pause(pauseTime)
		.exec(Catalog.Product.add)
		.pause(pauseTime)
		.exec(http("View Cart")
			.get("/cart/view"))
		.pause(pauseTime)
		.exec(http("Login User")
			.post("/login")
			.formParam("_csrf", "${csrf}")
			.formParam("username", "user1")
			.formParam("password", "pass"))
		.pause(pauseTime)
		.exec(http("Checkout")
			.get("/cart/checkout"))

	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}