package vio.controllers.requests

class HandleAnswerRequest(
    val phraseId: Int,
    val answer: String,
    val mode: String,
    val hintsCount: Int
)
