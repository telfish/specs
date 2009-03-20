package org.specs.matcher
import org.specs.Sugar._

class patternMatchersUnit extends MatchersSpecification {
  "A 'beLike' pattern matcher" should {
    "be ok even with a null pattern" in {
      val pattern : (String => Boolean) = null
      expectation("name" must beLike(pattern)) must failWith("'name' doesn't match the expected pattern")
    }
    "be ok even with a null value" in {
      val name : String = null
      expectation(name must beLike { case s: String => ok }) must failWith("'null' doesn't match the expected pattern")
    }
    "not evaluate the expressions twice" in {
      val anyValue: Any = 1
      beLike { (s:Any) => s match { case _ => ok } } must evalOnce(exp(anyValue))
    }
  }
  "A 'beSome' option matcher" should {
    "be ok even with a null value" in {
      val value : Option[String] = null
      expectation(value must beSome[String]) must failWith("'null' is not Some(x)")
    }
    "be ok even with a null pattern for the which function" in {
      val pattern : (Any => Boolean) = null
      expectation(Some("name") must beSome[String].which(pattern)) must failWith("the 'which' property is a null function")
    }
    "not evaluate the expressions twice" in {
      val some: Option[Int] = Some(1)
      beSome[Int] must evalOnce(exp(some))
    }
  }
  "A 'beNone' option matcher" should {
    "not evaluate the expressions twice" in {
      val nothing: Option[Any] = None
      beNone must evalOnce(exp(nothing))
    }
  }
}
