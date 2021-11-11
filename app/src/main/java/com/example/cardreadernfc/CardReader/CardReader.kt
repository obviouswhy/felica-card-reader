package com.example.waeoncardreadernfc.cardReader

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.nfc.tech.NfcF
import android.os.Message
import com.example.cardreadernfc.utils.Utils
import java.util.*


interface CardReaderInterface : CardReader.Listener {
    fun onReadTag(tag : Tag)
}

class CardReader(private val context: Context, private val activity: Activity): android.os.Handler() {
    private var nfcmanager : NfcManager? = null
    private var nfcadapter : NfcAdapter? = null
    private var callback : CustomReaderCallback? = null

    private var listener: CardReaderInterface? = null
    interface Listener {}

    fun start() {
        callback = CustomReaderCallback()
        callback?.setHandler(this)
        nfcmanager = context.getSystemService(Context.NFC_SERVICE) as NfcManager?
        nfcadapter = nfcmanager!!.getDefaultAdapter()
        nfcadapter!!.enableReaderMode(activity, callback
            ,NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,null)
    }

    fun stop(){
        nfcadapter!!.disableReaderMode(activity)
        callback = null
    }

    override fun handleMessage(msg: Message) {
        if (msg.arg1 == 1) {
            listener?.onReadTag(msg.obj as Tag)
        }
    }

    fun setListener(listener: CardReader.Listener?) {
        if (listener is CardReaderInterface) {
            this.listener = listener as CardReaderInterface
        }
    }

    private class CustomReaderCallback : NfcAdapter.ReaderCallback {
        private var handler : android.os.Handler? = null
        override fun onTagDiscovered(tag: Tag) {
            val msg = Message.obtain()
            msg.arg1 = 1
            msg.obj = tag
            if (handler != null) handler?.sendMessage(msg)
        }

        fun setHandler(handler  : android.os.Handler){
            this.handler = handler
        }
    }

}