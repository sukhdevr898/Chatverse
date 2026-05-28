tasks.register("downloadLogo") {
    doLast {
        val u = java.net.URL("https://raw.githubusercontent.com/sukhdevr898/Chatverse/refs/heads/main/file_000000001c287208ba6c4e5b58c752ff.png")
        u.openStream().use { input ->
            java.io.File("app/src/main/res/drawable").mkdirs()
            java.io.FileOutputStream("app/src/main/res/drawable/app_logo.png").use { output ->
                input.copyTo(output)
            }
        }
    }
}
