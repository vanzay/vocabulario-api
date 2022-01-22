package vio.controllers.requests

class AddPhrasesRequest(
    val bookId: Int,
    val onStudying: Boolean,
    val idList: List<Int>
)
