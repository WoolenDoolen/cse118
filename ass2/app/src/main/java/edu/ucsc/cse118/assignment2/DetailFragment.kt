package edu.ucsc.cse118.assignment2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.ucsc.cse118.assignment2.databinding.FragmentDetailBinding

class DetailFragment : Fragment(R.layout.fragment_detail) {
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val model = ViewModelProvider(requireActivity())[WorkspaceViewModel::class.java]
    val arguments = requireArguments()
    val message = model.workspaces[arguments.getInt("workspaceIndex")]
      .channels[arguments.getInt("channelIndex")].messages[arguments.getInt("messageIndex")]
    val binding = FragmentDetailBinding.bind(view)
    binding.date.text = message.formattedDate()
    binding.content.text = message.content
  }
}
