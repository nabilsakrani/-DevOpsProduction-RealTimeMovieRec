package it.reply.data.pasquali.controller

import java.io.File

import com.typesafe.config.ConfigFactory
import io.prometheus.client.{CollectorRegistry, Counter, Gauge}
import io.prometheus.client.exporter.PushGateway
import it.reply.data.pasquali.engine.MovieRecommenderEngine
import it.reply.data.pasquali.metrics.RecMetricsCollector
import it.reply.data.pasquali.metrics.model.{CounterMetric, Metric, TimerMetric}
import it.reply.data.pasquali.view.Template
import org.scalatra.scalate.ScalateSupport
import org.scalatra.{FlashMapSupport, ScalatraServlet}
import org.slf4j.LoggerFactory
import org.zeroturnaround.zip.ZipUtil

import scala.xml.Node

class Controller extends ScalatraServlet with FlashMapSupport with ScalateSupport{

  //var CONF_DIR = scala.util.Properties.envOrElse("DEVOPS_CONF_DIR", "conf")
  var CONF_DIR = "conf"
  var CONFIG_FILE = "application.conf"

  private def displayPage(title:String, content:Seq[Node]) =
    Template.page(title, content, url(_))

  var collabModel : MovieRecommenderEngine = null

  var MODEL_ARCHIVE_PATH : String = ""
  var MODEL_PATH : String = ""
  var MODEL_NAME : String = ""
  var SPARK_APPNAME : String = ""
  var SPARK_MASTER : String = ""

  var movieIDs : Array[Int] = Array()
  var userIDs : Array[Int] = Array()

  var userNodes : Array[Node] = Array()
  var movieNodes : Array[Node] = Array()

  val logger = LoggerFactory.getLogger(getClass)

  var pushGateway : PushGateway = null
  val registry = new CollectorRegistry

//  var gaugeUsersNumber : Gauge = null
//  var gaugeMoviesNumber : Gauge = null
//  var gaugeIsOnline : Gauge = null
//  var counterRequestsNumber : Counter = null
//  var gaugeDuration : Gauge = null
  var JOB_NAME = ""
  var ENV = ""
  var LABEL_USERS_NUMBER = ""
  var LABEL_MOVIES_NUMBER = ""
  var LABEL_REQUESTS_NUMBER = ""
  var LABEL_PROCESS_DURATION = ""

  def initSpark() : Unit = {

    val config = ConfigFactory.load()

    logger.info("------> Retrieve Conf variables <------")
    MODEL_ARCHIVE_PATH = config.getString("rtml.model.archive_path")
    MODEL_PATH = config.getString("rtml.model.path")
    MODEL_NAME = config.getString("rtml.model.name")
    SPARK_APPNAME = config.getString("rtml.spark.app_name")
    SPARK_MASTER = config.getString("rtml.spark.master")

    if(!new File(MODEL_ARCHIVE_PATH).exists())
      throw new IllegalArgumentException("The Zipped model doesn't exists")

    logger.info("------> Initialize Spark")

    collabModel = MovieRecommenderEngine()
      .init(SPARK_APPNAME, SPARK_MASTER)


    logger.info("------> ENV")
    logger.info("------>")
    logger.info(s"------> MODEL_ZIP_PATH = ${MODEL_ARCHIVE_PATH}")
    logger.info(s"------> MODEL_PATH = ${MODEL_PATH}")
    logger.info(s"------> MODEL_NAME = ${MODEL_NAME}")
    logger.info(s"------> SPARK_APP_NAME = ${SPARK_APPNAME}")
    logger.info(s"------> SPARK_MASTER = ${SPARK_MASTER}")


    logger.info("------> Spark Initialized")
    logger.info("------> Unzip ML Model")
    logger.info(s"------> FULL_MODEL_PATH = ${MODEL_PATH}${MODEL_NAME}")

    ZipUtil.unpack(new File(MODEL_ARCHIVE_PATH), new File(s"${MODEL_PATH}${MODEL_NAME}"))

    logger.info("------> Load ML Model")

    collabModel.loadCollaborativeModel(s"${MODEL_PATH}${MODEL_NAME}")

    logger.info("------> Populate Movies and Users Lists")

    userIDs = collabModel.model.userFeatures.sortByKey().collect().map(row => row._1)
    movieIDs = collabModel.model.productFeatures.sortByKey().collect().map(row => row._1)

//    userIDs.foreach(println)
//    movieIDs.foreach(println)

    userNodes = userIDs.map(id => <li>{id}</li>)
    movieNodes = movieIDs.map(id => <li>{id}</li>)

    //****************************************************************************

    ENV = config.getString("rtml.metrics.environment")
    JOB_NAME = config.getString("rtml.metrics.job_name")

//    val GATEWAY_ADDR = config.getString("rtml.metrics.gateway.address")
//    val GATEWAY_PORT = config.getString("rtml.metrics.gateway.port")

    LABEL_USERS_NUMBER = s"${config.getString("rtml.metrics.labels.users_number")}"
    LABEL_MOVIES_NUMBER = s"${config.getString("rtml.metrics.labels.movies_number")}"
    LABEL_REQUESTS_NUMBER = s"${config.getString("rtml.metrics.labels.requests_number")}"
    LABEL_PROCESS_DURATION = s"${config.getString("rtml.metrics.labels.process_duration")}"

    // *******************************************************************************


    RecMetricsCollector.addMetric(LABEL_USERS_NUMBER,
      new Metric(LABEL_USERS_NUMBER,
        "Number of users in the model", userIDs.length,JOB_NAME,ENV))

    RecMetricsCollector.addMetric(LABEL_MOVIES_NUMBER,
      new Metric(LABEL_MOVIES_NUMBER,
        "Number of movies in the model", movieIDs.length,JOB_NAME,ENV))

    RecMetricsCollector.addMetric(LABEL_REQUESTS_NUMBER,
      new CounterMetric(LABEL_REQUESTS_NUMBER,
        "Number of requests for a recommendation", JOB_NAME,ENV))

    RecMetricsCollector.addMetric(LABEL_PROCESS_DURATION,
      new TimerMetric(LABEL_PROCESS_DURATION,
        "Duration of last request", JOB_NAME, ENV))


    //****************************************************************************

    logger.info("------> IS ONLINE")
  }

  get("/") {

    if(collabModel == null)
    {
      initSpark()
    }

    val users = ""
    val movies = ""

    RecMetricsCollector.inc(LABEL_REQUESTS_NUMBER)

    displayPage("Movielens Recommender Instructions",

      <span>
        For now there isn't any form to insert the move you want to see.<br/>
        But you can use a very confrotable search in the address bar :D.<br/>
        <br/>

        Actually the movie recommender works fine with movie already seen by someone,
        obviously you can see a movie that noboy alreasy seen, but don't expect to get a significant advice.
        <br/><br/>

        So, choose a User and see a movie as him: <br/>
        <code>/see/userID/movieID</code><br/>

        or insert a new User id and start rate movies:<br/>
        <code>/see/newUserID/newMovieID/rate</code>
        <br/>
        <b>rate is a decimal value from 0.0 to 5.0</b>

      </span>

      <h4>Users</h4>
      <ul>
        { userNodes }
      </ul>

      <h4>Movies</h4>
      <ul>
        { movieNodes }
      </ul>
    )

  }

  get("/see/:user/:movie") {

    if(collabModel == null)
      initSpark()

    RecMetricsCollector.startTimer(LABEL_PROCESS_DURATION)

    val user = params.getOrElse("user", "-1").toInt
    val movie = params.getOrElse("movie", "-1").toInt

    val rate = collabModel.predictRating(user, movie)

    RecMetricsCollector.stopTimer(LABEL_PROCESS_DURATION)
    RecMetricsCollector.inc(LABEL_REQUESTS_NUMBER)

    displayPage("See a Movie",
      <span>
        User { user } see movie { movie }...<br/>
          ...and I suppose he rate it { rate }/5.0
      </span>
    )

  }

  get("/raw/see/:user/:movie") {

    if(collabModel == null)
      initSpark()

    RecMetricsCollector.startTimer(LABEL_PROCESS_DURATION)

    val user = params.getOrElse("user", "-1").toInt
    val movie = params.getOrElse("movie", "-1").toInt

    val rate = collabModel.predictRating(user, movie)

    RecMetricsCollector.stopTimer(LABEL_PROCESS_DURATION)
    RecMetricsCollector.inc(LABEL_REQUESTS_NUMBER)

    rate
  }

  get("/see/:user/:movie/:rate") {


    if(collabModel == null)
      initSpark()

    RecMetricsCollector.startTimer(LABEL_PROCESS_DURATION)

    val user = params.getOrElse("user", "-1").toInt
    val movie = params.getOrElse("movie", "-1").toInt
    val rate = params.getOrElse("rate", "0.0")

    val predict = collabModel.predictRating(user, movie)

    RecMetricsCollector.stopTimer(LABEL_PROCESS_DURATION)
    RecMetricsCollector.inc(LABEL_REQUESTS_NUMBER)

    logger.warn("Numero Richieste fino ad ora -> " + RecMetricsCollector.metrics(LABEL_REQUESTS_NUMBER)._value)

    displayPage("See a Movie",
      <span>
        User { user } see movie { movie } and rate it { rate }.
        The recommender supposed a rate of { predict }
      </span>
    )
  }

  get("/raw/see/:user/:movie/:rate") {

    if(collabModel == null)
      initSpark()

    RecMetricsCollector.startTimer(LABEL_PROCESS_DURATION)

    val user = params.getOrElse("user", "-1").toInt
    val movie = params.getOrElse("movie", "-1").toInt
    val rate = params.getOrElse("rate", "0.0")

    val predict = collabModel.predictRating(user, movie)

    RecMetricsCollector.stopTimer(LABEL_PROCESS_DURATION)
    RecMetricsCollector.inc(LABEL_REQUESTS_NUMBER)

    s"$predict - $rate"
  }

  get("/isOnline") {
    "is Online"
  }

  get("/metrics") {
    RecMetricsCollector.getPrometheusMetrics()
  }

}

