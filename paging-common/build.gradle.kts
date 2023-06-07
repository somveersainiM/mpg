import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("maven-publish")
}

kotlin {
  ios()
  iosSimulatorArm64()
  js(IR) {
    browser()
  }
  jvm()

  sourceSets {
    all {
      languageSettings {
        optIn("androidx.paging.ExperimentalPagingApi")
        optIn("kotlin.RequiresOptIn")
      }
    }
    val commonMain by getting {
      dependencies {
        implementation(libs.kotlin.stdlib.common)
        implementation(libs.kotlinx.coroutines.core)
      }
    }
    val nonJsMain by creating {
      dependsOn(commonMain)
    }
    val jvmMain by getting {
      dependsOn(nonJsMain)
      dependencies {
        api(libs.androidx.paging.common)
      }
    }
    val nonJvmMain by creating {
      kotlin.srcDir("../upstreams/androidx-main/paging/paging-common/src/commonMain")
      dependsOn(commonMain)
      dependencies {
        implementation(libs.stately.concurrency)
        implementation(libs.stately.iso.collections)
      }
    }
    val iosMain by getting {
      kotlin.srcDir("../upstreams/androidx-main/paging/paging-common/src/nonJsMain", )
      dependsOn(nonJsMain)
      dependsOn(nonJvmMain)
    }
    val iosSimulatorArm64Main by getting {
      dependsOn(iosMain)
    }
    val jsMain by getting {
      kotlin.srcDir("../upstreams/androidx-main/paging/paging-common/src/jsMain", )
      dependsOn(nonJvmMain)
    }
  }
}


publishing {

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
