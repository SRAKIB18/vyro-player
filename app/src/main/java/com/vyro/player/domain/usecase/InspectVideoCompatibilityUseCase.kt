package com.vyro.player.domain.usecase

import com.vyro.player.decoder.CodecCompatibilityInspector
import com.vyro.player.domain.model.CompatibilityReport
import javax.inject.Inject

class InspectVideoCompatibilityUseCase @Inject constructor(
    private val codecCompatibilityInspector: CodecCompatibilityInspector
) {
    operator fun invoke(pathOrUri: String): CompatibilityReport {
        return codecCompatibilityInspector.inspectFile(pathOrUri)
    }
}
