package edu.ucsc.cse118.assignment2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import edu.ucsc.cse118.assignment2.databinding.FragmentListBinding

class MessagesFragment : Fragment(R.layout.fragment_list) {
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val workspaceIndex = requireArguments().getInt("workspaceIndex")
    val channelIndex = requireArguments().getInt("channelIndex")
    val model = ViewModelProvider(requireActivity())[WorkspaceViewModel::class.java]
    val messages = model.workspaces[workspaceIndex].channels[channelIndex].messages
    val binding = FragmentListBinding.bind(view)
    binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
    binding.recyclerview.adapter = MessageAdapter(messages) { index ->
      val arguments = Bundle().apply {
        putInt("workspaceIndex", workspaceIndex)
        putInt("channelIndex", channelIndex)
        putInt("messageIndex", index)
      }
      findNavController().navigate(R.id.action_messages_to_detail, arguments)
    }
  }
}
