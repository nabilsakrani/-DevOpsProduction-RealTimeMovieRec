name := "RealTimeMovieRec"

version := "0.1"

scalaVersion := "2.11.11"

val ScalatraVersion = "2.6.+"
val sparkVersion = "2.2.0"

resolvers ++= Seq(
  "All Spark Repository -> bintray-spark-packages" at "https://dl.bintray.com/spark-packages/maven/"
)

libraryDependencies ++= Seq(
  "org.scalatra"            %% "scalatra"                     % ScalatraVersion,
  "org.scalatra"            %% "scalatra-scalate"             % ScalatraVersion,
  "org.scalatra"            %% "scalatra-specs2"              % ScalatraVersion     % "test",
  "org.eclipse.jetty"       %  "jetty-webapp"                 % "9.4.6.v20170531"   % "provided",
  "javax.servlet"           %  "javax.servlet-api"            % "3.1.0"             % "provided",

  "org.scalatra"            %% "scalatra-scalatest"           % "2.6.2"             % "test",

  "org.apache.spark"        %  "spark-core_2.11"              % "2.2.0",
  "org.apache.spark"        %  "spark-sql_2.11"               % "2.2.0",
  "org.apache.hadoop"       %  "hadoop-mapreduce-client-core" % "2.7.2",
  "org.apache.hadoop"       %  "hadoop-client"                % "2.7.2",
  "org.apache.hadoop"       %  "hadoop-common"                % "2.7.0",
  "org.apache.spark"        %  "spark-hive_2.11"              % "2.2.0",
  "org.apache.spark"        %  "spark-yarn_2.11"              % "2.2.0",
  "org.apache.kudu"         %  "kudu-spark2_2.11"             % "1.5.0",
  "org.apache.spark"        %  "spark-mllib_2.11"             % "2.2.0",
  "com.typesafe"            %  "config"                       % "1.3.2"
)

enablePlugins(JettyPlugin)
//enablePlugins(SbtTwirl)

//containerPort in Jetty := 10000

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}