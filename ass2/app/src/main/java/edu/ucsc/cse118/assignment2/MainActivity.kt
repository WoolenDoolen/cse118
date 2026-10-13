// Original activity scaffold supplied by David C. Harrison.
package edu.ucsc.cse118.assignment2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(R.layout.activity_main)
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    val model = ViewModelProvider(this)[WorkspaceViewModel::class.java]
    val host = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
    val controller = host.navController
    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    toolbar.setNavigationOnClickListener { controller.navigateUp() }
    controller.addOnDestinationChangedListener { _, destination, arguments ->
      val workspaceIndex = arguments?.getInt("workspaceIndex") ?: 0
      val channelIndex = arguments?.getInt("channelIndex") ?: 0
      val messageIndex = arguments?.getInt("messageIndex") ?: 0
      toolbar.title = when (destination.id) {
        R.id.workspacesFragment -> getString(R.string.select_workspace)
        R.id.channelsFragment -> model.workspaces[workspaceIndex].name
        R.id.messagesFragment -> model.workspaces[workspaceIndex].channels[channelIndex].name
        R.id.detailFragment -> model.workspaces[workspaceIndex]
          .channels[channelIndex].messages[messageIndex].user.name
        else -> getString(R.string.app_name)
      }
      toolbar.navigationIcon = if (destination.id == R.id.loginFragment) null
        else getDrawable(R.drawable.ic_back)
      toolbar.navigationContentDescription = getString(R.string.back)
    }
  }
}
