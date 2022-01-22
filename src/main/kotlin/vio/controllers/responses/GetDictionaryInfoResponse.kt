package vio.controllers.responses

import vio.domain.Language
import vio.domain.results.DictionarySummary

class GetDictionaryInfoResponse(
    val dictionary: DictionarySummary,
    val language: Language
)
