    /**
     * payment code for surcharge**/
    internal const val SURHARGE_CODE_1 = "1075" //transfer between 1 and 5000
    internal const val SURHARGE_CODE_2 = "2688" //transfer between 5001 and 50,000
    internal const val SURHARGE_CODE_3 = "5375" //transfer between 50,001 and above
    internal const val SURHARGE_CODE_6105 = "2500" //transfer between 50,001 and above

    fun getSurchargeFromETT(eTTx: ETTx, amount: String? = "0"): String {
        return  when (eTTx) {
            ETTx.ETT_6103 -> SURHARGE_CODE_1
            ETTx.ETT_6104 -> getBanded6104Surcharge( ETTx.ETT_6104, amount?.toInt())
            ETTx.ETT_6105 -> SURHARGE_CODE_6105
        }
    }

    fun getBanded6104Surcharge(tier:ETTx, amount: Int?): String {
            return when (tier) {
                ETTx.ETT_6104 -> {
                    when(amount!!){
                        in 0..1200 -> "1200"
                        in 2001..8000 -> "2200"
                        else -> "3000"
                    }
                }
                else -> {
                    when(amount!!){
                        in 100..500000 -> SURHARGE_CODE_1
                        in 500001..5000000 -> SURHARGE_CODE_2
                        else -> {
                            SURHARGE_CODE_3
                        }
                    }
                }
            }
    }


enum class ETTx(var ettName: String) {
	 ETT_6103(ettName = "6103"),
	 ETT_6104(ettName = "6104"),
	 ETT_6105(ettName = "6105")
}


