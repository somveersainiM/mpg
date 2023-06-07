import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost

plugins {
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.mavenPublish) apply false
  id("maven-publish")
}

allprojects {
  group = "app.cash.paging"
  version = "${rootProject.libs.versions.androidx.paging.get()}-0.1.2"

  repositories {
    mavenCentral()
    google()
  }

}

publishing {
  publications.withType<MavenPublication> {
    this.artifactId = "${rootProject.name}-${project.name}"
  }

  publications {
    repositories {
      maven {
        /** Configure path of your package repository on Github
         *  Replace GITHUB_USERID with your/organisation Github userID and REPOSITORY with the repository name on GitHub
         */
        setUrl(uri("https://gitlab.com/api/v4/projects/29868680/packages/maven"))

        credentials(HttpHeaderCredentials::class) {
          name = "Deploy-Token"
          value = "RFxnv5NkW-Afn5NRtDpS"
        }
        authentication {
          create<HttpHeaderAuthentication>("header")
        }
      }
    }
  }
}
