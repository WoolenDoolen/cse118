package edu.ucsc.cse118.assignment2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import edu.ucsc.cse118.assignment2.databinding.FragmentLoginBinding

class LoginFragment : Fragment(R.layout.fragment_login) {
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    FragmentLoginBinding.bind(view).loginButton.setOnClickListener {
      findNavController().navigate(R.id.action_login_to_workspaces)
    }
  }
}
