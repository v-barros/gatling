package computerdatabase

import scala.concurrent.duration._
import scala.util.Random
import io.gatling.core.Predef._
import io.gatling.http.Predef._


class TestSimulation extends Simulation {

  val port =  System.getProperty("port","1080")
  val hostUsers = System.getProperty("hostUsers","null")
  val hostProducts = System.getProperty("hostProducts","null")
  val nothing = System.getProperty("nothingFor","30").toInt.seconds
  val users = System.getProperty("users","1000").toInt
  val rampDuration = System.getProperty("rampDuration","500").toInt.seconds
  val numberOfRamps = System.getProperty("numberOfRamps","5").toInt
  val steadyPeriodDuration = System.getProperty("steadyDuration","5").toInt.minutes
  val finalPeriodDuration = System.getProperty("finalDuration","10").toInt.minutes
  val httpProtocol = http   


  object User {
      val min = 1000000000
      val max = 1999999999

      val emailFeeder = Iterator.continually(Map("email" -> (Random.alphanumeric.take(20).mkString + "@test.com")))
      val nameFeeder = csv("users.csv").circular
      val taxIdFeeder = Iterator.continually(Map("taxId" -> (Random.nextInt(max-min)+min)))
      val nickNameFeeder = Iterator.continually(Map("nickName" -> (Random.alphanumeric.take(20).mkString)))
      
      def createUser ={
          feed(emailFeeder)
          .feed(nameFeeder)
          .feed(taxIdFeeder)
          .feed(nickNameFeeder)
          .exec(  
                http("Create user")
                .post(hostUsers+":"+port+"/user/registration")
                .headers(Map("Content-type"->"application/json"))
                .body(StringBody(
                    """{
                        "email":"${email}",
                        "password":"112233",
                        "name":"${name}",
                        "taxId":"${taxId}",
                        "gender":"M",
                        "nickName":"${nickName}"
                        }""".stripMargin)).asJson
                .check(jsonPath("$.id").saveAs("userId"))
          )
      }

      def auth = {
          exec(  
                http("Auth user")
                .post(hostUsers+":"+port+"/auth")
                .headers(Map("Content-type"->"application/json"))
                .body(StringBody(
                    """{
                        "email":"${email}",
                        "password":"112233"
                        }""".stripMargin)).asJson
                .check(jsonPath("$.token").saveAs("token"))
          )
      }
      def getUser = {
          exec(  
                  http("Get user details")
                  .get(hostUsers+":"+port+"/user/${userId}")
                  .headers(Map("Content-type"->"application/json",
                                "Authorization" -> "Bearer ${token}"))
            )
      }
  }

  object Product {
      val productTitleFeeder = Iterator.continually(Map("productTitle" -> (Random.alphanumeric.take(20).mkString)))
      val searchProductsFeeder = Iterator.continually(Map("searchParam" -> (Random.alphanumeric.take(2).mkString)))

      def createProduct ={
          feed(productTitleFeeder)
          .exec(  
                http("Create product")
                .post(hostProducts+":"+port+"/products")
                .headers(Map("Content-type"->"application/json",
                              "Authorization" -> "Bearer ${token}"))
                .body(StringBody(
                    """{
                      "title": "${productTitle}",
                      "description": "sadasdasdasdasda",
                      "fiatPrice": 1000.09,
                      "ownerId": "${userId}",
                      "productCategory": "SPORTS"
                      }""".stripMargin)).asJson

                .check(jsonPath("$.id").saveAs("productId"))
          )
      }

      def productPage = {
        exec(  
               http("Product Page")
               .get(hostProducts+":"+port+"/products/${productId}")
               .headers(Map("Authorization" -> "Bearer ${token}"))
            )
      }

      def listProducts = {
          exec(  
               http("List Products")
               .get(hostProducts+":"+port+"/products")
               .headers(Map("Authorization" -> "Bearer ${token}"))
            )
      }
      
      def searchProducts = {
          feed(searchProductsFeeder)
          .exec(  
               http("Search Products")
               .get(hostProducts+":"+port+"/products/search?searchText=${searchParam}")
               .headers(Map("Authorization" -> "Bearer ${token}"))
            )
      }
  }

  // A scenario is a chain of requests and pauses
  val scn = scenario("Scenario Name")
    .exec(User.createUser)
    .exec(User.auth)
    .exec(User.getUser)
    .exec(Product.createProduct)
    .exec(Product.listProducts)
    .exec(Product.searchProducts)
    .exec(Product.productPage)

  setUp(
  scn.inject(
    //nothingFor(nothing),
    // generate a closed workload injection profile
    // with levels of 10, 15, 20, 25 and 30 concurrent users
    // each level lasting 10 seconds
    // separated by linear ramps lasting 10 seconds
    incrementConcurrentUsers(users)
      .times(numberOfRamps)
      .eachLevelLasting(steadyPeriodDuration)
      .separatedByRampsLasting(rampDuration)
      .startingFrom(0)
  ).protocols(httpProtocol)
)

}
