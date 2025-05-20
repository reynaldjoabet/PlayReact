
import play.sbt.PlayRunHook
import sbt._
import java.net.InetSocketAddress
import scala.sys.process.Process

object UIRunHook {
  def apply(base: File): PlayRunHook = {

    object NpmProcess extends PlayRunHook {

      var watchProcess: Option[Process] = None

      override def afterStarted(): Unit = {
        // don't run "npm start" directly as it leaves zombie node.js child processes on termination
        watchProcess = Some(
          Process(
            "npm start", base, "EXTEND_ESLINT" -> "true"
          ).run()
        )
      }

      override def afterStopped(): Unit = {
        println("[sbt log] Shutting down UI...")
        watchProcess foreach( _.destroy() )
        watchProcess = None
      }
    }

    NpmProcess
  }
}