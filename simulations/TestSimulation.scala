/*
 * Copyright 2011-2021 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package computerdatabase

import scala.concurrent.duration._
import scala.util.Random
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class TestSimulation extends Simulation {

 
  val httpProtocol = http
    // Here is the root for all relative URLs
    .baseUrl("http://localhost:3000")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
  
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
                .post("/user/registration")
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
                .post("/auth")
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
                  .get("/user/${userId}")
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
                .post("/products")
                .headers(Map("Content-type"->"application/json",
                              "Authorization" -> "Bearer ${token}"))
                .body(StringBody(
                    """{
                      "title": "${productTitle}",
                      "description": "sadasdasdasdasda",
                      "price": 1000.09,
                      "ownerId": "${userId}",
                      "productCategory": "SPORTS"
                      }""".stripMargin)).asJson

                .check(jsonPath("$.id").saveAs("productId"))
          )
      }

      def productPage = {
        exec(  
               http("Product Page")
               .get("/products/${productId}")
               .headers(Map("Authorization" -> "Bearer ${token}"))
            )
      }

      def listProducts = {
          exec(  
               http("List Products")
               .get("/products")
               .headers(Map("Authorization" -> "Bearer ${token}"))
            )
      }
      
      def searchProducts = {
          feed(searchProductsFeeder)
          .exec(  
               http("Search Products")
               .get("/products/search?searchText=${searchParam}")
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
    nothingFor(1.seconds),
   atOnceUsers(1) 
  )).protocols(httpProtocol)


}
