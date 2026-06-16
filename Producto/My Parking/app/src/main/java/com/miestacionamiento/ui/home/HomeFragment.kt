package com.miestacionamiento.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miestacionamiento.R
import com.miestacionamiento.databinding.FragmentHomeBinding
import com.miestacionamiento.utils.gone
import com.miestacionamiento.utils.visible

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private val popularAdapter = ParkingHorizontalAdapter { parking ->
        findNavController().navigate(
            HomeFragmentDirections.actionHomeToDetail(parking.id)
        )
    }

    private val recentAdapter = ParkingHorizontalAdapter { parking ->
        findNavController().navigate(
            HomeFragmentDirections.actionHomeToDetail(parking.id)
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvPopular.adapter = popularAdapter
        binding.rvRecent.adapter = recentAdapter

        viewModel.popularParkings.observe(viewLifecycleOwner) { list ->
            popularAdapter.submitList(list.take(6))
        }

        viewModel.recentlyViewed.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvRecentTitle.gone()
                binding.rvRecent.gone()
            } else {
                binding.tvRecentTitle.visible()
                binding.rvRecent.visible()
                recentAdapter.submitList(list)
            }
        }

        binding.etSearch.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (query.length >= 2) {
                viewModel.search(query).observe(viewLifecycleOwner) { results ->
                    popularAdapter.submitList(results)
                }
            } else if (query.isEmpty()) {
                viewModel.popularParkings.observe(viewLifecycleOwner) { list ->
                    popularAdapter.submitList(list.take(6))
                }
            }
        }

        binding.etSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                findNavController().navigate(R.id.exploreFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
