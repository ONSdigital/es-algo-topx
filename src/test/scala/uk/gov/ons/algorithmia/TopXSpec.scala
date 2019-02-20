package uk.gov.ons.algorithmia

import org.scalatest._

class TopXSpec extends FlatSpec with Matchers {
  "Initial uk.gov.ons.spark.TopX algorithm" should "return Hello plus input" in {
    val algorithm = new TopX()
    "Hello Bob" shouldEqual algorithm.apply("Bob")
  }
}
