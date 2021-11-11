package com.example.cardreadernfc.utils

import java.io.ByteArrayOutputStream
import java.lang.StringBuilder
import java.util.*

object Utils {
    @JvmStatic
    fun byteToHex(b : ByteArray) : String {
        var s : String = ""
        for (i in 0..b.size-1){
            s += "%02X".format(b[i])
        }
        return s
    }
    fun isWaon(sysCode : String) : Boolean {
        return sysCode == "12FC" || sysCode == "852B"
    }
    // readWithoutEncryption method from [https://m-shige1979.hatenablog.com/entry/2015/09/30/080000, https://qiita.com/zaburo/items/d76567086dfcaeaf3e72#mainactivityjava]
    fun readWithoutEncryption(idm: ByteArray, blocksize: Int): ByteArray? {
        val bout = ByteArrayOutputStream()
        bout.write(0) // Size of the byte array, value will be added later
        bout.write(0x06) // Read Without Encryption [4-4-5 from: https://www.sony.net/Products/felica/business/tech-support/data/card_usersmanual_2.11e.pdf]
        bout.write(idm)
        bout.write(1) // Number of Service [4-4-5 from: https://www.sony.net/Products/felica/business/tech-support/data/card_usersmanual_2.11e.pdf]
        bout.write(0x4f) // Service Code List (684F) second byte
        bout.write(0x68) // Service Code List first byte [https://www-wdic-org.translate.goog/w/CUL/WAON?_x_tr_sl=ja&_x_tr_tl=en&_x_tr_hl=ja&_x_tr_pto=nui]
        bout.write(blocksize) // Number of blocks (2 blocks for Service 684F) [https://www-wdic-org.translate.goog/w/CUL/WAON?_x_tr_sl=ja&_x_tr_tl=en&_x_tr_hl=ja&_x_tr_pto=nui]
        for (i in 0 until blocksize) {
            bout.write(0x80)
            bout.write(i)
        }
        val msg: ByteArray = bout.toByteArray()
        msg[0] = msg.size.toByte() // Size of the byte array in the first position
        return msg
    }

}
