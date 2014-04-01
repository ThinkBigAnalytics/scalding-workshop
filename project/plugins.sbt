resolvers += Classpaths.typesafeResolver

resolvers += Resolver.url("plugins-artifactory", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

//addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.1.2")

//addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.2.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.10.1")
