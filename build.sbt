name := "todolist"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  cache
)     

play.Project.playJavaSettings

