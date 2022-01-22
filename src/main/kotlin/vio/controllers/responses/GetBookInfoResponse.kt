package vio.controllers.responses

import vio.domain.Book
import vio.domain.UserBook

class GetBookInfoResponse(
    val book: Book,
    val summary: UserBook?
)
