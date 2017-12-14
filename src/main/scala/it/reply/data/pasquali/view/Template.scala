package it.reply.data.pasquali.view

import scala.xml.Node

object Template {

  def page(title:String,
           content:Seq[Node],
           url: String => String = identity _,
           head: Seq[Node] = Nil,
           scripts: Seq[String] = Seq.empty,
           defaultScripts: Seq[String] = Seq("/assets/js/jquery.min.js", "/assets/js/bootstrap.min.js")) = {

    <html lang="en">
      <head>
        <title>{ title }</title>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <meta name="description" content="Real Time Movielens Recommender" />
        <meta name="author" content="Dario Pasquali" />

        <link rel="stylesheet" type="text/css" href="//maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css" />
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
        <script type="text/javascript" src="//maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
        <link rel="stylesheet" type="text/css" href="/assets/css/main.css" />

        <!-- Le styles
        <link href="/assets/css/bootstrap.css" rel="stylesheet" />
        <link href="/assets/css/bootstrap-responsive.css" rel="stylesheet" />
        <link href="/assets/css/syntax.css" rel="stylesheet" />
        <link href="/assets/css/scalatra.css" rel="stylesheet" />-->

        <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
        <!--[if lt IE 9]>
          <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
        <![endif]-->
        {head}
      </head>

      <body>

        <!--
        <div class="navbar navbar-inverse navbar-fixed-top">
          <div class="navbar-inner">
            <div class="container">
              <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
              </a>
              <a class="brand" href="/">Scalatra Examples</a>
              <div class="nav-collapse collapse">

              </div>
            </div>
          </div>
        </div>-->

        <nav class="navbar navbar-default navbar-static-top navbar-inverse">
          <div class="container">
            <ul class="nav navbar-nav">
              <li class="active">
                <a href="/"><span class="glyphicon glyphicon-home"></span> Home</a>
              </li>
              <li>
                <a href="https://en.wikipedia.org/wiki/DevOps"><span class="glyphicon glyphicon-question-sign"></span> What is DevOps</a>
              </li>
              <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false"><span class="glyphicon glyphicon-info-sign"></span>  Project Docs <span class="caret"></span></a>
                <ul class="dropdown-menu" role="menu">
                  <li><a href="">System Architecture</a></li>
                  <li><a href="">Dev processes</a></li>
                  <li><a href="">Cloudera Cluster</a></li>
                  <li><a href="">Automated Tests</a></li>
                  <li class="divider"></li>
                  <li><a href="">Configuration Management</a></li>
                  <li><a href="">Continuous Integration</a></li>
                  <li><a href="">Continuous Testing</a></li>
                  <li><a href="">Infrastructure as a Code</a></li>
                  <li><a href="">Continuous Deployment</a></li>
                  <li><a href="">Continuous Delivery</a></li>
                  <li><a href="">Continuous Management</a></li>
                </ul>
              </li>
            </ul>
            <ul class="nav navbar-nav navbar-right">
              <li class="navbar-right">
                <a href="https://github.com/dpasqualiReply/DevOpsProduction">
                  <span class="glyphicon icon-github"></span> GitHub Repository</a>
              </li>
            </ul>
          </div>
        </nav>

        <div class="jumbotron text-center">
          <div class="container">
            <a href="/">
              <img src="/assets/img/movielens-logo-white.png"/>
            </a>
          </div>
        </div>

        <div class="row bottomPadding">

          <div class="col-md-4">
            <h3><span class="glyphicon glyphicon-info-sign"></span> How this sample app works</h3>
            This is a simple webapp build with Scalatra framework, it will show che capabilities of <b>DevOps</b>.
            <br/>
            The app give you advices on movies basing on what did you, and other user, see.
            The core engine is a Collaborative-filtering recommender implemented with <b>Spark MLlib</b>.
            <br/>
            New features will be implemented day by day, so Stay Tuned!!
          </div>

          <div class="col-md-8">
            <h3><span class="glyphicon glyphicon-film"></span> What is the next movie??</h3>

            { content }

          </div>



        </div>




        <!--<div class="container">
          <div class="content">
            <div class="page-header">
              <h1>{ title }</h1>
            </div>
            <div class="row">
              <div class="span3">
                <ul class="nav nav-list">-->
                  <!--<li><a href={url("/cookies-example")}>Cookies example</a></li>-->
<!--                  <li><a href="/">Hello world</a></li>
                </ul>
              </div>
              <div class="span9">
                {content}
              </div>
              <hr/>
            </div>
          </div>
        </div>
-->
        <footer class="vcard" role="contentinfo">

        </footer>

        <!-- Le javascript
          ================================================== -->
        <!-- Placed at the end of the document so the pages load faster -->
        { (defaultScripts ++ scripts) map { pth =>
        <script type="text/javascript" src={pth}></script>
      } }

      </body>

    </html>
  }

}
