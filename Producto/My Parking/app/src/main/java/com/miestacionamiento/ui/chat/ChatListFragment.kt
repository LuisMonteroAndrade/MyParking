package com.miestacionamiento.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.miestacionamiento.R
import com.miestacionamiento.databinding.FragmentChatListBinding
import com.miestacionamiento.utils.gone
import com.miestacionamiento.utils.visible

class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ConversationAdapter(0) { conv ->
            val action = ChatListFragmentDirections
                .actionChatListToChat(conv.id, conv.parkingName ?: "Chat")
            findNavController().navigate(action)
        }

        binding.rvConversations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvConversations.adapter = adapter

        viewModel.userId.observe(viewLifecycleOwner) { uid ->
            (binding.rvConversations.adapter as? ConversationAdapter)?.let {
                val newAdapter = ConversationAdapter(uid) { conv ->
                    val action = ChatListFragmentDirections
                        .actionChatListToChat(conv.id, conv.parkingName ?: "Chat")
                    findNavController().navigate(action)
                }
                binding.rvConversations.adapter = newAdapter
                viewModel.conversations.value?.let { list -> newAdapter.submitList(list) }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) binding.progressBar.visible() else binding.progressBar.gone()
        }

        viewModel.conversations.observe(viewLifecycleOwner) { list ->
            (binding.rvConversations.adapter as? ConversationAdapter)?.submitList(list)
            if (list.isEmpty()) {
                binding.layoutEmpty.visible()
                binding.rvConversations.gone()
            } else {
                binding.layoutEmpty.gone()
                binding.rvConversations.visible()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadConversations()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
