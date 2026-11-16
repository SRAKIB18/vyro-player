package com.vyro.player.domain.usecase

import com.vyro.player.decoder.DecoderCapabilityInspector
import com.vyro.player.domain.model.DecoderCapability
import javax.inject.Inject

class GetDecoderCapabilitiesUseCase @Inject constructor(
    private val decoderCapabilityInspector: DecoderCapabilityInspector
) {
    operator fun invoke(): List<DecoderCapability> = decoderCapabilityInspector.getDecoderCapabilities()
}
