import com.typesafe.config.ConfigFactory
import io.prometheus.client.{CollectorRegistry, Gauge}
import io.prometheus.client.exporter.PushGateway
import it.reply.data.pasquali.controller.Controller
import it.reply.data.pasquali.metrics.RecMetricsCollector
import it.reply.data.pasquali.metrics.model.Metric
import org.apache.http.HttpResponse
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener


object JettyLauncher { // this is my entry object as specified in sbt project definition
  def main(args: Array[String]) {
    val port = if(System.getenv("PORT") != null) System.getenv("PORT").toInt else 10000

    val resBase = "src/main/webapp"
    //val resBase = "/opt/devops/realtime_ml/webapp"

    val server = new Server(port)
    val context = new WebAppContext()
    context.setContextPath("/")
    context.setResourceBase(resBase)
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[Controller], "/")

    server.setHandler(context)

    server.start
    //server.join
    scala.io.Source.fromURL(s"http://localhost:$port/").mkString


    val config = ConfigFactory.load()

    val ENV = config.getString("rtml.metrics.environment")
    val JOB_NAME = config.getString("rtml.metrics.job_name")

    val LABEL_IS_ONLINE = s"${config.getString("rtml.metrics.labels.service_is_online")}"

    val isOnline = scala.io.Source.fromURL(s"http://localhost:$port/isOnline").mkString

    var io = 0

    if(isOnline.contains("is Online"))
      io = 1
    else
      io = 0

    RecMetricsCollector.addMetric(LABEL_IS_ONLINE,
      new Metric(LABEL_IS_ONLINE,
        "1 if is the recommendation service is online, 0 otherwise", io,JOB_NAME,ENV))




  }
}