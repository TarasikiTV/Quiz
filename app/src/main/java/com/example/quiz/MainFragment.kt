package com.example.quiz

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.quiz.databinding.FragmentMainBinding
import com.google.gson.Gson
import org.json.JSONObject
import kotlin.system.exitProcess

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private val BASE_URL =
        "https://gist.githubusercontent.com/TarasikiTV/1bbf9348c3c0a4d74eed2dabc04b894d/raw/gistfile1.txt"
    private var answer: String = ""
    private var answerSelected = false
    private var isAnswerGiven = false
    private var questionNumber: Int = 0
    private var scoreTrueAnswer: Int = 0
    private var scoreAllQuestion: Int = 0
    private val TIME_INTERVAL = 2000
    private var backPressed: Long = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startProgressBar()
        loadDataFromServer(questionNumber)
        binding.bottomNextQuestion.setOnClickListener {
            if (isAnswerGiven && scoreAllQuestion<5) {
                isAnswerGiven = false
                animationNextQuestion()
            } else if(scoreAllQuestion<5) {
                Toast.makeText(
                    context,
                    "Спочатку оберіть правильну відповідь",
                    Toast.LENGTH_SHORT
                ).show()
            }
            when (scoreAllQuestion) {

                5 ->sendDataToRestartMenu(scoreTrueAnswer.toString())
            }
        }

    }
    private fun sendDataToRestartMenu(data: String) {
        val bundle = Bundle()
        bundle.putString("key_data", data)
        val fragment = ResultFragment()
        fragment.arguments = bundle
        findNavController().navigate(R.id.resultFragment, bundle)
    }
    private fun animationNextQuestion() {
        val cardView1 = binding.CardViewQuestion1
        cardView1.animate()
            .translationX(+cardView1.width.toFloat())
            .alpha(0f)
            .setDuration(400)
            .withEndAction {
                cardView1.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(400)
                    .start()
                loadDataFromServer(++questionNumber)
            }
            .start()
    }

    private fun startProgressBar() {
        val progressBar = binding.progressBar
        val handler = Handler()
        val runnable = object : Runnable {
            var progress = 0

            override fun run() {
                progress += 10
                progressBar.progress = progress

                if (progress < 100) {
                    handler.postDelayed(this, 200) // Повторяем через 500 миллисекунд
                } else {
                    progressBar.visibility = View.GONE
                }
            }
        }
        handler.postDelayed(runnable, 100)
    }

    private fun loadDataFromServer(questionNumber: Int) {
        binding.CardViewQuestion1.visibility = View.GONE
        val request = JsonObjectRequest(Request.Method.GET, BASE_URL, null,
            { response ->
                handleServerResponse(response, questionNumber)
                binding.CardViewQuestion1.visibility = View.VISIBLE
            },
            { error ->
                // Обработка ошибки
                println("Ошибка при запросе: ${error.message}")
            })
        Volley.newRequestQueue(requireContext()).add(request)

    }

    private fun handleServerResponse(response: JSONObject, questionNumber: Int) {

        val gson = Gson()
        val questionResponse = gson.fromJson(response.toString(), QuestionResponse::class.java)
        val quizQuestions = questionResponse.questions

        val firstQuestion = quizQuestions[questionNumber]
        setupQuestion(firstQuestion)
        answer = firstQuestion.True_variant
    }

    private fun setupQuestion(question: Question) {
        answerSelected = false
        binding.textViewVariant1.setTextColor(Color.WHITE)
        binding.textViewVariant2.setTextColor(Color.WHITE)
        binding.textViewVariant3.setTextColor(Color.WHITE)
        binding.textViewVariant4.setTextColor(Color.WHITE)
        binding.textViewQuestion.text = question.question

        val variants = listOf(
            question.variant_1,
            question.variant_2,
            question.variant_3,
            question.variant_4
        ).shuffled()

        variants.forEachIndexed { index, variant ->
            val textView = when (index) {
                0 -> binding.textViewVariant1
                1 -> binding.textViewVariant2
                2 -> binding.textViewVariant3
                3 -> binding.textViewVariant4
                else -> null
            }

            textView?.text = variant

            textView?.setOnClickListener {
                if (!answerSelected) {
                    val selectedVariant = textView.text.toString()
                    if (selectedVariant == answer) {
                        textView.setTextColor(Color.GREEN)
                        scoreTrueAnswer++
                    } else {
                        textView.setTextColor(Color.RED)
                    }
                    answerSelected = true
                    isAnswerGiven = true
                    scoreAllQuestion++
                }
            }
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