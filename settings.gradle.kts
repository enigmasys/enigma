rootProject.name = "enigma"
include("common")
include("secretapp")
include("command")
include("api")
include("client")


val archivaHostId: String? by settings
val archivaPort: String? by settings

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        archivaPort?.let {
            archivaHostId?.let {
                maven {
                    isAllowInsecureProtocol = true
                    url = uri("http://$archivaHostId:$archivaPort/repository/snapshots")
                }
            }
        }
    }
}
include("client")
include("client")
