# SeeThroughVisualizer

Partie Mobile du projet CD-SeeThrough. Cette application android permet de
récupérer le flux UDP extrait du projet
[quadcopter-ros](https://github.com/AymericCassard/quadcopter-ros) et
l'insère dans l'image a la place d'une planche ChArUco, créant une
illusion de "SeeThrough".

Le projet est compilé avec Android Studio, Java 25. 

Issu de app/build.gradle:

```
android {
    namespace 'com.ac_pfe.seethroughvisualizer'
    compileSdkVersion 36

    defaultConfig {
        applicationId "com.ac_pfe.seethroughvisualizer"
        minSdk 24
        targetSdkVersion 32
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
    }
}
```

Donc:
- compilé pour android SDK 32 -> Android 12
- Min Sdk compatibilité 24 -> Android 7
- Java 11 <= pour compiler le projet

Ouvrir le projet dans Android Studio devrait suffire pour compiler/lancer.

## Générer une planche ChArUco

Voir [quadcopter-ros](https://github.com/AymericCassard/quadcopter-ros),
`aruco_tools/generate_charuco.py`
