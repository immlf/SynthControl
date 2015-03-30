package ua.pp.lab101.synthesizercontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.text.DecimalFormatSymbols;

import ua.pp.lab101.synthesizercontrol.ADRegisters.ADBoardController;

/**
 * Fragment that presents Constant frequency operation mode.
 * User just sets frequency and starts the synthesizer.
 * The fragment does not shuts down the synthesizer when loses focus.
 */
public class ConstantModeFragment extends Fragment {
    /*Constants*/
    private static final String TAG = "SynthesizerControl";

    /*View elements: */
    private ToggleButton mToggleBtn = null;
    private EditText mFrequencyValue;

    /*System elements. Context and usbdevice*/
    private static Context mDeviceConstantModeContext;
    private D2xxManager mFtdid2xx = null;
    private FT_Device mFtDev = null;
    private int mDevCount = -1;

    /**/
    private ADBoardController adf;
    /*Logic workflow variables*/
    public ConstantModeFragment() {
        // Required empty public constructor
    }

    /*Overloaded consrtuctor. This anit good but we need contxt and FTDManager to control
    * the device*/
    public ConstantModeFragment(Context parentContext, D2xxManager ftdid2xx) {
        this.mFtdid2xx = ftdid2xx;
        mDeviceConstantModeContext = parentContext;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.constant_mode, container, false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mDeviceConstantModeContext.getApplicationContext().registerReceiver(mUsbPlugEvents, filter);
        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mToggleBtn = (ToggleButton) getActivity().findViewById(R.id.applyBtn);
        mFrequencyValue = (EditText) getActivity().findViewById(R.id.frequencyValue);
        mFrequencyValue.setFilters(new InputFilter[] {
                new DigitsKeyListener(Boolean.FALSE, Boolean.TRUE) {
                    int beforeDecimal = 4, afterDecimal = 3;

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        DecimalFormatSymbols dfs = new DecimalFormatSymbols();

                        String ds = String.valueOf(dfs.getDecimalSeparator());
                        String temp = mFrequencyValue.getText() + source.toString();

                        if (temp.equals(ds)) {
                            return "0".concat(ds);
                        }
                        else if (temp.toString().indexOf(ds) == -1) {
                            if (temp.length() > beforeDecimal) {
                                return "";
                            }
                        } else {
                            temp = temp.substring(temp.indexOf(ds) + 1);
                            if (temp.length() > afterDecimal) {
                                return "";
                            }
                        }

                        return super.filter(source, start, end, dest, dstart, dend);
                    }
                }
        });
        if (mToggleBtn != null) {
            mToggleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonSendPressed();
                }
            });
        }
    }

    @Override
    public void onStart() {
        adf = ADBoardController.getInstance();
        super.onStart();
        mDevCount = -1;
        ConnectFunction();
    }

    @Override
    public void onStop() {
        if (mFtDev != null && mFtDev.isOpen()) {
            mFtDev.close();
        }
        super.onStop();
    }

    public void ConnectFunction() {
        int openIndex = 0;
        if (mDevCount > 0)
            return;

        mDevCount = mFtdid2xx.createDeviceInfoList(mDeviceConstantModeContext);
        if (mDevCount > 0) {
            mFtDev = mFtdid2xx.openByIndex(mDeviceConstantModeContext, openIndex);

            if (mFtDev == null) {
                //Toast.makeText(mDeviceConstantModeContext, "mftDev == null", Toast.LENGTH_LONG).show();
                showToast("mftDev == null");
                return;
            }

            if (true == mFtDev.isOpen()) {
                mFtDev.resetDevice();
                mFtDev.setBaudRate(115200);
                mFtDev.setLatencyTimer((byte) 16);
                mFtDev.setBitMode((byte) 0x0f, D2xxManager.FT_BITMODE_ASYNC_BITBANG);
                //Toast.makeText(mDeviceConstantModeContext,
                //        "devCount:" + mDevCount + " open index:" + openIndex,
                //        Toast.LENGTH_SHORT).show();
                showToast( "devCount:" + mDevCount + " open index:" + openIndex);
            } else {
//                Toast.makeText(mDeviceConstantModeContext,
//                        "Need to get permission!", Toast.LENGTH_SHORT).show();
                showToast("Need to get permission!");
            }
        } else {
            Log.e("j2xx", "mDevCount <= 0");
        }
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//
//    }

    /*Main logic methods*/
    public void buttonSendPressed() {
        if (mFtDev != null) {
            if (mToggleBtn.isChecked()) {
                double frequencyValue = Double.parseDouble(mFrequencyValue.getText().toString());

                if ((frequencyValue < 35) || (frequencyValue > 4400)) {
                    showToast("Frequency value not is in range");
                    mToggleBtn.setChecked(false);
                    return;
                }

                Log.i(TAG, "Value to be set: " + Double.toString(frequencyValue) + " MHz");
                writeData(adf.geiInitianCommanSequence());
                writeData(adf.setFrequency(frequencyValue));
                writeData(adf.turnOnDevice());
            } else {
                writeData(adf.turnOffTheDevice());
                Log.i(TAG, "Button toggled off");
            }
        } else {
            Log.e(TAG, "No device present.");
            mToggleBtn.setChecked(false);
        }
    }

    private void writeData(byte[][] commands) {
        for (int i = 0; i < commands.length; i++) {
            int result = writeDataToRegister(commands[i]);
            Log.i(TAG, Integer.toString(result) + " bytes wrote to reg" + Integer.toString(i));
        }
    }

    private int writeDataToRegister(byte[] data) {
        int result = mFtDev.write(data);
        return result;
    }

    private void showToast(String textToShow) {
        Toast toast = Toast.makeText(mDeviceConstantModeContext,
                textToShow,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /*Broadcast receiver realization for hotplug realization*/
    private BroadcastReceiver mUsbPlugEvents = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                try {
                    mDevCount = -1;
                    mFtDev = null;
                    showToast("Device disconnected!");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    String s = e.getMessage();
                    if (s != null) {
                        //Error_Information.setText(s);
                    }
                    e.printStackTrace();
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                showToast("Device attached!");
            }
        }
    };

}
