package com.miestacionamiento.ui.saved

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.miestacionamiento.databinding.FragmentSavedBinding
import com.miestacionamiento.utils.gone
import com.miestacionamiento.utils.visible

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SavedViewModel by viewModels()

    private val adapter = SavedParkingAdapter(
        onItemClick = { parking ->
            findNavController().navigate(
                SavedFragmentDirections.actionSavedToDetail(parking.id)
            )
        },
        onRemoveClick = { parking -> viewModel.removeFromSaved(parking) }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvSaved.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSaved.adapter = adapter

        viewModel.savedParkings.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isEmpty()) {
                binding.layoutEmpty.visible()
                binding.rvSaved.gone()
            } else {
                binding.layoutEmpty.gone()
                binding.rvSaved.visible()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
