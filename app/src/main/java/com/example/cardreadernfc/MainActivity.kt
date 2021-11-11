package com.example.cardreadernfc

import android.nfc.Tag
import android.nfc.tech.NfcF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.core.view.isVisible
import com.example.cardreadernfc.utils.Utils
import com.example.waeoncardreadernfc.cardReader.CardReader
import com.example.waeoncardreadernfc.cardReader.CardReaderInterface
import java.util.*

class MainActivity : AppCompatActivity() {
    val cardReader = CardReader(this, this)
    var toggle: ToggleButton? = null
    var scanningIndicator: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanningIndicator = findViewById<LinearLayout>(R.id.scanningIndicator)
        scanningIndicator?.isVisible = false

        toggle = findViewById<ToggleButton>(R.id.toggleButton)
        toggle?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                println("---- Start Scanning ----")
                scanningIndicator?.isVisible = true
                cardReader.start()
            } else {
                println("---- Stop Scanning ----")
                scanningIndicator?.isVisible = false
                cardReader.stop()
            }
        }
        cardReader.setListener(cardListener)
    }

    /*
    public override fun onStart() {
        super.onStart()
        toggle?.isChecked = true
    }

    override fun onResume() {
        super.onResume()
        cardReader.start()
    }
    * */

    override fun onPause() {
        super.onPause()
        toggle?.isChecked = false
    }


    private val cardListener = object : CardReaderInterface {
        override fun onReadTag(tag : Tag) {
            tag.techList

            val idm : ByteArray = tag.id
            val nfc : NfcF = NfcF.get(tag) ?: return
            val systemCode : ByteArray = nfc.systemCode
            val manufacturer  : ByteArray = nfc.manufacturer
            val idmLabel = findViewById<TextView>(R.id.idm)
            val isWaonLabel = findViewById<TextView>(R.id.isWaonLabel)
            val systemCodeLabel = findViewById<TextView>(R.id.systemCode)
            val manufacturerCodeLabel = findViewById<TextView>(R.id.manufacturerCode)

            Toast.makeText(this@MainActivity, "Card Detected", Toast.LENGTH_SHORT).show()

            idmLabel.text = "IDm: \n${Utils.byteToHex(idm)}"
            systemCodeLabel.text = "System Code: \n${Utils.byteToHex(systemCode)}"
            isWaonLabel.text = "isWaon: ${Utils.isWaon(Utils.byteToHex(systemCode))}"
            manufacturerCodeLabel.text = "Manufacturer: \n${Utils.byteToHex(manufacturer)}"

            println("_____________onReadTag_______________")
            println("idm -> ${Utils.byteToHex(idm)}")
            println("systemCode -> ${Utils.byteToHex(systemCode)}")
            println("manufacturer -> ${Utils.byteToHex(manufacturer)}")


            if (Utils.isWaon(Utils.byteToHex(systemCode))) {
                println("_____________GET WAON NUMBER_______________")
                try {
                    nfc.connect()

                    val polling_request = byteArrayOf(
                        0x06.toByte(), // Packet size (6 bytes for Read Without Encryption) [https://wiki.onakasuita.org/pukiwiki/?FeliCa%2Fコマンド%2FRead%20Without%20Encryption], [4-4-5 from: https://www.sony.net/Products/felica/business/tech-support/data/card_usersmanual_2.11e.pdf]
                        0x00.toByte(), // Command code (00h -> polling) [Table 2-3 from: https://www.sony.net/Products/felica/business/tech-support/data/card_usersmanual_2.11e.pdf]
                        0xFE.toByte(), 0x00.toByte(), // System code (FE00 for WAON, 0003 for SUICA/PASMO/etc)
                        0x00.toByte(), // Request code (00h -> No Request) [4-4-2 Packet structure from: https://www.sony.net/Products/felica/business/tech-support/data/card_usersmanual_2.11e.pdf]
                        0x00.toByte()  // Timeslot [4-4-2 from: https://www.sony.net/Products/felica/business/tech-support/data/card_usersmanual_2.11e.pdf]
                    )

                    val polling_response = nfc.transceive(polling_request)
                    val idm_from_polling_response: ByteArray = Arrays.copyOfRange(polling_response, 2, 10) // idm => 8bits starting from 2nd (1st is Response Code)  [4-4-2 ResponsePacketData from: https://www.sony.net/Products/felica/business/tech-support/data/card_usersmanual_2.11e.pdf]
                    // val pmm = Arrays.copyOfRange(polling_response, 11, 19) // pmm => 8bits starting after idm
                    val waon_number_request: ByteArray? = Utils.readWithoutEncryption(idm_from_polling_response, 2)
                    val waon_number_response = nfc.transceive(waon_number_request)
                    val waon_number = Arrays.copyOfRange(waon_number_response, 13, 21) // Block Data starts after the 12nd byte [4-4-5 from: https://www.sony.net/Products/felica/business/tech-support/data/card_usersmanual_2.11e.pdf]
                    println("waon number -> ${Utils.byteToHex(waon_number)}")
                    isWaonLabel.text = "isWaon: ${Utils.isWaon(Utils.byteToHex(systemCode))} \nWaon Number: \n${Utils.byteToHex(waon_number)}"

                    nfc.close()
                } catch (e : Exception) {
                    nfc.close()
                }

            }
            println("_______________________________________________")
        }

    }
}