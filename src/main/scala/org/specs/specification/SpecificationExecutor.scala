package org.specs.specification
import org.specs.util.Classes._
import org.specs.util.{ Configuration }
/**
 * This trait executes an example by cloning the enclosing specification first.
 * 
 * This way, the example is executed in a total isolation so as not to share local variables between examples
 * and avoid side-effects.
 * 
 * Warning: this works by considering that the "examples" method is stable on a BaseSpecification and
 * will always return the same examples in the same order
 */
trait SpecificationExecutor extends ExampleLifeCycle { this: BaseSpecification with ExampleExpectationsListener =>
  /** execute a sus by cloning the specification and executing the cloned sus */
  override def executeSus(sus: Sus): this.type = {
    if (!oneSpecInstancePerExample || systems(0) == sus) // || executeOneExampleOnly 
      sus.execution()
    else {
      cloneSpecification match {
        case None => sus.execution()
        case Some(s) => {
            val index = this.systems.zipWithIndex.find(_._1 eq sus).map(_._2).getOrElse(-1)
            if (index < s.systems.size) {
              s.executeOneExampleOnly = true
              s.expectationsListener = this
              s.parentLifeCycle = this
              val cloned = s.systems(index)
              cloned.execution()
              sus.exampleList = cloned.exampleList
              sus.exampleList.foreach(_.cycle = sus)
              sus.before = cloned.before
              sus.after = cloned.after
              sus.untilPredicate = cloned.untilPredicate
              sus.firstActions = cloned.firstActions
              sus.lastActions = cloned.lastActions
              sus.failedSus = cloned.failedSus
              sus.executed = true
              sus.examplesFilter = cloned.examplesFilter
              s.executeOneExampleOnly = false
            }
        }
      }
    }
    this
  }
  /** execute an example by cloning the specification and executing the cloned example */
  override def executeExample(example: Example): this.type = {
    var executed = false
    try {
      val path  = example.pathFromRoot
      if (oneSpecInstancePerExample && !executeOneExampleOnly && !path.isFirst) {
        cloneSpecification match {
          case None => example.executeThis
          case Some(s) => {
            s.executeOneExampleOnly = true
            s.parentLifeCycle = this
            val cloned = s.getExample(path)
            cloned match {
              case None => throw PathException(path + "not found for " + example)
              case Some(c) => {
                c.tagWith(this)
                c.execution.execute
                example.copyExecutionResults(c)
              }
            }
            s.expectationsListener = this
            executed = true
          }
        }
      }
    } catch { 
      case e: PathException => throw e
      case _ => ()
    }
    if (!executed)
      example.execution.execute
    
    this
  }
  /** @return a clone of the specification */
  private[specification] def cloneSpecification = {
    tryToCreateObject[BaseSpecification with ExpectableFactory](getClass.getName, false, false)
  }
}
case class PathException(m: String) extends Exception(m)