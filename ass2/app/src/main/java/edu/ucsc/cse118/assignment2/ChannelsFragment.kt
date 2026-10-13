package edu.ucsc.cse118.assignment2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import edu.ucsc.cse118.assignment2.databinding.FragmentListBinding

class ChannelsFragment : Fragment(R.layout.fragment_list) {
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val workspaceIndex = requireArguments().getInt("workspaceIndex")
    val model = ViewModelProvider(requireActivity())[WorkspaceViewModel::class.java]
    val binding = FragmentListBinding.bind(view)
    binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
    binding.recyclerview.adapter = ListAdapter(model.workspaces[workspaceIndex].channels.map {
      it.name to getString(R.string.message_count, it.messages.size)
    }) { index ->
      val arguments = Bundle().apply {
        putInt("workspaceIndex", workspaceIndex)
        putInt("channelIndex", index)
      }
      findNavController().navigate(R.id.action_channels_to_messages, arguments)
    }
  }
}
