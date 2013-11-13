name := "todolist"

version := "1.0-SNAPSHOT"

//resolvers += "Apache" at "http://repo1.maven.org/maven2/"

   resolvers ++= Seq(
        "Apache" at "http://repo1.maven.org/maven2/",
        Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
        Resolver.url("Objectify Play Snapshot Repository", url("http://schaloner.github.com/snapshots/"))(Resolver.ivyStylePatterns)
        )



libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "org.apache.httpcomponents" % "httpclient" % "4.3",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "commons-lang" % "commons-lang" % "2.6",
  "com.typesafe" %% "play-plugins-mailer" % "2.2.0",
  "net.sf.opencsv" % "opencsv" % "2.3",
  "com.amazonaws" % "aws-java-sdk" % "1.6.2",
  "com.google.guava" % "guava" % "r09",
  "mysql" % "mysql-connector-java" % "5.1.6",
  "be.objectify" %% "deadbolt-java" % "2.2-RC2",
  "net.sf.supercsv" % "super-csv" % "2.1.0",
  cache
)     

play.Project.playJavaSettings

