package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.content.ContextWrapper
import android.content.Context

private val ChatVerseColorScheme = darkColorScheme(
    primary = NeonBlue,
    secondary = ElectricPurple,
    tertiary = CyanGlow,
    background = DeepBlack,
    surface = Graphite,
    onPrimary = DeepBlack,
    onSecondary = DeepBlack,
    onTertiary = DeepBlack,
    onBackground = PureWhite,
    onSurface = PureWhite,
    error = ErrorRed
)

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Lock to dark theme for AMOLED
  dynamicColor: Boolean = false, // Disable dynamic colors
  content: @Composable () -> Unit,
) {
  val colorScheme = ChatVerseColorScheme

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = view.context.findActivity()?.window
      if (window != null) {
          window.statusBarColor = android.graphics.Color.TRANSPARENT
          window.navigationBarColor = android.graphics.Color.TRANSPARENT
          val insetsController = WindowCompat.getInsetsController(window, view)
          insetsController.isAppearanceLightStatusBars = false
          insetsController.isAppearanceLightNavigationBars = false
      }
    }
    
    DisposableEffect(view) {
      val window = view.context.findActivity()?.window
      if (window != null) {
          val insetsController = WindowCompat.getInsetsController(window, view)
          insetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
          insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
      }
      onDispose {}
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
