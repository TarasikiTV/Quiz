package com.example.quiz

data class Question(
val question: String,
val variant_1 :String,
val variant_2 :String,
val variant_3 :String,
val variant_4 :String,
val True_variant :String,
)
data class QuestionResponse(
    val name: String,
    val questions: List<Question>
)