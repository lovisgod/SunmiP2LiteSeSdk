package com.lovisgod.sunmip2litese.domain.use_cases

data class AllUseCases(
   val downloadAid: DownloadAidUseCase,
   val downloadCapkUseCase: DownloadCapkUseCase,
   val setTerminalConfigUseCase: SetTerminalConfigUseCase,
   val setPinKeyUseCase: SetPinKeyUseCase,
   val emvPayUseCase: EmvPayUseCase,
   val printBitMapUseCase: PrintBitMapUseCase,
   val writePinkeyUseCase: WritePinkeyUseCase,
   val loadMasterKeyUseCase: LoadMasterKeyUseCase,
   val getDeviceSerialUseCase: GetDeviceSerialUseCase
)