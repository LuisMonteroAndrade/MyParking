package com.miestacionamiento.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.miestacionamiento.MiEstacionamientoApp
import com.miestacionamiento.databinding.FragmentExploreBinding
import kotlinx.coroutines.launch

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExploreViewModel by viewModels()

    private val adapter = ParkingAdapter(
        onItemClick = { parking ->
            findNavController().navigate(
                ExploreFragmentDirections.actionExploreToDetail(parking.id)
            )
        },
        onSaveClick = { parking ->
            val repo = (requireActivity().application as MiEstacionamientoApp).repository
            viewModel.viewModelScope.launch {
                repo.toggleSaved(parking.id, !parking.isSaved)
            }
        }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvParkings.adapter = adapter

        viewModel.parkings.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvCount.text = "${list.size} estacionamientos encontrados"
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setQuery(newText.orEmpty())
                return true
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
