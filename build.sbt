name := "neutrino"

version := "1.0-SNAPSHOT"

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
  "org.apache.commons" % "commons-exec" % "1.2",
  "org.apache.commons" % "commons-lang3" % "3.3.1",
  "com.typesafe" %% "play-plugins-mailer" % "2.2.0",
  "com.amazonaws" % "aws-java-sdk" % "1.6.2",
  "com.google.guava" % "guava" % "r09",
  "com.google.gdata" % "core" % "1.47.1",
  "mysql" % "mysql-connector-java" % "5.1.6",
  "be.objectify" %% "deadbolt-java" % "2.2-RC2",
  "org.pojava" % "pojava" % "2.9.0",
  "org.polyjdbc" % "polyjdbc" % "0.3.0",
  "c3p0" % "c3p0" % "0.9.1.2",
  "org.sql2o" % "sql2o" % "1.3.0",
  "com.googlecode.juniversalchardet" % "juniversalchardet" % "1.0.3",
  cache
)     

play.Project.playJavaSettings
