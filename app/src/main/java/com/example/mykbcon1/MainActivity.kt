package com.example.mykbcon1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mykbcon1.databinding.ActivityMainBinding
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvPacketIBeacon
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvType
import com.kkmcn.kbeaconlib2.KBCfgPackage.KBAdvMode
import com.kkmcn.kbeaconlib2.KBCfgPackage.KBAdvTxPower
import com.kkmcn.kbeaconlib2.KBCfgPackage.KBCfgAdvIBeacon
import com.kkmcn.kbeaconlib2.KBConnPara
import com.kkmcn.kbeaconlib2.KBConnState
import com.kkmcn.kbeaconlib2.KBeacon
import com.kkmcn.kbeaconlib2.KBeacon.ConnStateDelegate
import com.kkmcn.kbeaconlib2.KBeaconsMgr
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mKBeaconsMgr: KBeaconsMgr
    private lateinit var myAdapter:Adapter
    private val lstt= mutableListOf<KBeacon>()
    private val password:String="0000000000000000"
    private var timestamp = 0.0
    private var last_time = 0.0
    private var ctr=0
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 1
//    private lateinit var connectionDelegate:ConnStateDelegate

    private val PERMISSION_COARSE_LOCATION = 22
    private val PERMISSION_FINE_LOCATION = 23
    private val PERMISSION_SCAN = 24
    private val PERMISSION_CONNET=25
    private var mScanFailedContinueNum = 0
    private val MAX_ERROR_SCAN_NUMBER = 2
    private val deviceList= mutableListOf<KBeacon>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myAdapter= Adapter()
        setUpRecyclerView()
        mKBeaconsMgr= KBeaconsMgr.sharedBeaconManager(this)
        if (mKBeaconsMgr == null) {
            toastShow("make sure the phone has support ble funtion")
            finish()
            return
        }
        binding.btnScan.setOnClickListener {
            deviceList.clear()

//            myAdapter.differ.submitList(deviceList)
//            myAdapter.notifyDataSetChanged()
            startScanning()
        }

        mKBeaconsMgr.delegate=beaconMgrExample
        myAdapter.setOnItemClickListener {
//            it.connect(password,20000L,beaconMgrExample)
//            it.connect(password,20000,connectionDelegate)
//            Log.d("hello",it.isSupportSensorDataNotification.toString()+"sensor data notification support")

//            connectToDevice(it)
//            if(it.getTriggerCfg(0)==null){
//                Log.d("hello","device is clicked")
//            }
//            Log.d("hello",it.getTriggerCfg(0).toString()+"trigger")
////            Log.d("hello",it.sensorRecordsMgr.toString()+"sensor record mgr")
            Log.d("hello",it.batteryPercent.toString()+"battery percent")


            binding.rvMain.visibility= View.GONE
            binding.llReading.visibility= View.VISIBLE
            binding.btnScan.visibility= View.GONE
            toastShow("successfully connected to the device ${it.name}")
            Log.d("Aamir","we are connected ${it.isConnected} from on create")
//            if(it.isConnected)countTouchAfterConnection(it)
//            Log.d("james",it.readt)
        }
    }



    private fun checkBluetoothPermitAllowed(): Boolean {
        var bHasPermission = true
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_FINE_LOCATION
            )
            bHasPermission = false
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_COARSE_LOCATION
            )
            bHasPermission = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                    PERMISSION_SCAN
                )
                bHasPermission = false
            }
            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_CONNECT)
                !=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    PERMISSION_CONNET
                )
                bHasPermission=false
            }
        }



        return bHasPermission
    }

    override  fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        if (requestCode == PERMISSION_SCAN) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                toastShow("The app need ble scanning permission for start ble scanning")
            }
        }
        if (requestCode == PERMISSION_COARSE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                toastShow("The app need coarse location permission for start ble scanning")
            }
        }
        if (requestCode == PERMISSION_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                toastShow("The app need fine location permission for start ble scanning")
            }
        }


    }


    private fun toastShow(strMsg: String?) {
        val toast = Toast.makeText(this, strMsg, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    override fun onStop() {
        super.onStop()
        mKBeaconsMgr.stopScanning()
    }

    override fun onDestroy() {
        super.onDestroy()
        mKBeaconsMgr.stopScanning()
    }
    private fun startScanning(){
        if (!checkBluetoothPermitAllowed()) {
            toastShow("Please allowed all permission")
            return
        }

        val nScan=mKBeaconsMgr.startScanning()


       when(nScan){
           0->Log.d("hello","scanning started")
           else-> toastShow("please allowed all permission to start scanning")
       }
    }

    var beaconMgrExample: KBeaconsMgr.KBeaconMgrDelegate = object : KBeaconsMgr.KBeaconMgrDelegate {
        //get advertisement packet during scanning callback
        override fun onBeaconDiscovered(beacons: Array<KBeacon>) {
//            lstt.addAll(beacons)
//            Log.d("hello",lstt.size.toString()+"listt size")
            for (beacon in beacons) {

//                if(beacon.mac == "BC:57:29:03:6D:BF"){
                //get beacon adv common info
                deviceList.add(beacon)
                myAdapter.differ.submitList(deviceList)
//                myAdapter.notifyDataSetChanged()
//                Log.d("james",beacon.triggerCapability().toString()+" trigger")
                if(beacon.mac=="BC:57:29:03:6D:BF"){
                    Log.v("hello", "beacon mac:" + beacon.mac)
                    Log.v("hello", "beacon name:" + beacon.name)
                    Log.v("hello", "beacon rssi:" + beacon.rssi)
                }

//                Log.v("hello", "beacon mac:" + beacon.mac)
//                Log.v("hello", "beacon name:" + beacon.name)
//                Log.v("hello", "beacon rssi:" + beacon.rssi)

                timestamp = Date().time.toDouble()
                if (timestamp - last_time >= 30000) {
                    last_time = timestamp
                    ///Log.d("b10 scan", "b10 scan try catch major " + String.valueOf(iBeaconFrame.getMajor()) + " " + major + " " + String.valueOf(minewFrame.getCurSlot()) + " timestamp " + String.valueOf(timestamp) + " diff = " + String.valueOf((timestamp - timestamp_prev) / 1000));
                    ctr++
                    binding.etSys.text=ctr.toString()
                }


                //get adv packet
//                for (advPacket in beacon.allAdvPackets()) {
//                    when (advPacket.advType) {
//                        KBAdvType.IBeacon -> {
//                            val advIBeacon = advPacket as KBAdvPacketIBeacon
//                            Log.v("hello", "iBeacon uuid:" + advIBeacon.uuid)
//                            Log.v("hello", "iBeacon major:" + advIBeacon.majorID)
//                            Log.v("hello", "iBeacon minor:" + advIBeacon.minorID)
//                        }
//
////
//
//                        else -> {}
//                    }
//                }
//                }



                //clear all buffered packet
                beacon.removeAdvPacket()
            }
//            myAdapter.differ.submitList(deviceList)
        }

        override fun onCentralBleStateChang(nNewState: Int) {
            if (nNewState == KBeaconsMgr.BLEStatePowerOff) {
                Log.e("hello", "BLE function is power off")
            } else if (nNewState == KBeaconsMgr.BLEStatePowerOn) {
                Log.e("hello", "BLE function is power on")
            }

        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("hello", "Start N scan failed：$errorCode")
            if (mScanFailedContinueNum >= MAX_ERROR_SCAN_NUMBER) {
                toastShow("scan encount error, error time:$mScanFailedContinueNum")
            }
            mScanFailedContinueNum++
        }
    }

    private var  connectionDelegate = object : ConnStateDelegate{
        override fun onConnStateChange(beacon: KBeacon?, state: KBConnState?, nReason: Int) {
            when(state){
//                KBConnState.Connected-> Log.d("Aamir","Device is connected successfully")
                KBConnState.Connected->{
                    //if we do something here
                    Log.d("hello","connected successfully ${beacon!!.isConnected}")
//                    countTouchAfterConnection(beacon)

//                    updateToIbecon(beacon)
                }
                //here may be some hack if device is connected we will only advertise the device when the clicked is happen
                //we may change the config after device is connected
//                KBConnState.Connected-> readDeviceOutput(beacon!!)
//                KBConnState.Connected-> Log.d("Aamir","connected successfully ${beacon!!.isConnected} to ${beacon!!.name}")
                KBConnState.Connecting->Log.d("Aamir","Device is connecting")
                KBConnState.Disconnected->Log.d("Aamir","Device Disconnected")
                else ->Log.d("Aamir","there is error while connecting the device")

            }
        }

    }

    private fun setUpRecyclerView(){
        binding.rvMain.apply {
            adapter=myAdapter
            layoutManager= LinearLayoutManager(this@MainActivity)
        }
    }
    private fun connectToDevice(device:KBeacon){
        device.connect(password,20000,connectionDelegate)
        val kBeaconPara=KBConnPara()

        kBeaconPara.readTriggerPara=true
//        if(kBeaconPara.readTriggerPara){
//            Log.d("tom","Device is clicked ")
//        }
        device.connectEnhanced(password,20000,kBeaconPara,connectionDelegate)
    }
    private fun readDeviceOutput(device: KBeacon){
        val commonCfg=device.commonCfg

        val slot=device.getSlotCfg(0)
//        Log.d("hello",slot.toString()+"slott issue")

        if(commonCfg!=null){
            Log.d("output", "support iBeacon:" + commonCfg.isSupportIBeacon());
            Log.d("output", "support eddy url:" + commonCfg.isSupportEddyURL());
            Log.d("output", "support eddy tlm:" + commonCfg.isSupportEddyTLM());
            Log.d("output", "support eddy uid:" + commonCfg.isSupportEddyUID());
            Log.d("output", "support ksensor:" + commonCfg.isSupportKBSensor());
            Log.d("output", "beacon has button:" + commonCfg.isSupportButton());
            Log.d("output", "beacon can beep:" + commonCfg.isSupportBeep());
            Log.d("output", "support acceleration sensor:" + commonCfg.isSupportAccSensor());
            Log.d("output", "support humidity sensor:" + commonCfg.isSupportHumiditySensor());
            Log.d("output", "support PIR sensor:" + commonCfg.isSupportPIRSensor());
//            Log.d("output", "support  sensor:" + commonCfg.isSupportSensor());
            Log.d("output", "support light sensor:" + commonCfg.isSupportLightSensor());
            Log.d("output", "support light sensor:" + commonCfg.isSupportLightSensor());
            Log.d("output", "support VOC sensor:" + commonCfg.isSupportVOCSensor());
            Log.d("output", "support max tx power:" + commonCfg.getMaxTxPower());
            Log.d("output", "support min tx power:" + commonCfg.getMinTxPower());
        }
    }
    private fun countTouchAfterConnection(device: KBeacon){
        if(device.isConnected){
            val iBeaconCfg = KBCfgAdvIBeacon()
//            iBeaconCfg.uuid="B9407F30-F5F8-466E-AFF9-25556B57FE67"
//            Toast.makeText(this@MainActivity,iBeaconCfg.uuid,Toast.LENGTH_SHORT).show()
            iBeaconCfg.isAdvTriggerOnly=true
//            Log.d("output",iBeaconCfg.uuid+"uuid")
//            Log.d("output",iBeaconCfg.isAdvTriggerOnly.toString())
            Log.d("hello","connected successfully ${device.isConnected}")


            //also stop the scanning after got connected
//            mKBeaconsMgr.stopScanning()
            device.disconnect()
            if(!device.isConnected){
                Log.d("hello","we are successfully disconnect the device ${!device.isConnected}")
            }

        }
        else Log.d("hello","not connected ${device.isConnected}")

    }
    private fun updateToIbecon(device: KBeacon){
        if(!device.isConnected)return
        val iBeaconCfg=KBCfgAdvIBeacon()
        //slot index
        iBeaconCfg.slotIndex = 0;
        iBeaconCfg.advMode = KBAdvMode.Legacy;

        //set the device to connectable.
        iBeaconCfg.isAdvConnectable = true;

        //always advertisement
        iBeaconCfg.isAdvTriggerOnly = true;

        //adv period and tx power
        iBeaconCfg.advPeriod = 1280.0f;
        iBeaconCfg.txPower = KBAdvTxPower.RADIO_Neg4dBm;

        //iBeacon para
        for (advPacket in device.allAdvPackets()) {
            when (advPacket.advType) {
                KBAdvType.IBeacon -> {
                    val advIBeacon = advPacket as KBAdvPacketIBeacon
//                    Log.v("hello", "iBeacon uuid:" + advIBeacon.uuid)
//                    Log.v("hello", "iBeacon major:" + advIBeacon.majorID)
//                    Log.v("hello", "iBeacon minor:" + advIBeacon.minorID)
                    iBeaconCfg.uuid=advIBeacon.uuid
                    iBeaconCfg.majorID=advIBeacon.majorID
                    iBeaconCfg.minorID=advIBeacon.minorID
                }

//

                else -> {}
            }
        }
        device.disconnect()
        if(!device.isConnected){
            Log.d("hello",device.triggerCfgList.size.toString()+"list size")
            for(p in device.triggerCfgList){
                Log.d("hello",p.triggerAction.toString()+"trigger action")
            }
        }

    }

}