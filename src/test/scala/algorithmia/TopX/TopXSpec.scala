package algorithmia.TopX

import org.scalatest._

class TopXSpec extends FlatSpec with Matchers {
  "Initial TopX algorithm" should "return Hello plus input" in {
    val algorithm = new TopX()
    "Hello Bob" shouldEqual algorithm.apply("Bob")
  }
}
