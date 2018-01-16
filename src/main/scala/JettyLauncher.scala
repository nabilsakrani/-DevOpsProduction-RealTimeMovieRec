import it.reply.data.pasquali.controller.Controller
import org.apache.http.HttpResponse
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener


object JettyLauncher { // this is my entry object as specified in sbt project definition
  def main(args: Array[String]) {
    val port = if(System.getenv("PORT") != null) System.getenv("PORT").toInt else 10000

    val server = new Server(port)
    val context = new WebAppContext()
    context.setContextPath("/")
    context.setResourceBase("src/main/webapp")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[Controller], "/")

    server.setHandler(context)

    server.start
    //server.join
    scala.io.Source.fromURL("http://localhost:10000/").mkString
  }
}