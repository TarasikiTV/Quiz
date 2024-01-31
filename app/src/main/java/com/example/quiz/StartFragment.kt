package com.example.quiz

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.example.quiz.databinding.FragmentStartBinding
import kotlin.system.exitProcess

class StartFragment : Fragment() {
    private lateinit var binding: FragmentStartBinding
    private val TIME_INTERVAL = 2000
    private var backPressed: Long = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonStartQuiz.setOnClickListener {
        findNavController().navigate(R.id.mainFragment)

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                    activity?.finish()
                    exitProcess(0)
                } else {
                    Toast.makeText(context, "Натисніть ще раз для виходу", Toast.LENGTH_SHORT).show()
                }
                backPressed = System.currentTimeMillis()
            }
        })
    }
}