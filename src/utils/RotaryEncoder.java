/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dawid
 */
public class RotaryEncoder {
    
    Logger logger = Logger.getLogger("RoateryEncoder");

    private final GpioPinDigitalInput inputA;
    private final GpioPinDigitalInput inputB;
    private final GpioController gpio;

    private long encoderValue = 0;
    private int lastEncoded = 0;
    private boolean firstPass = true;

    private RotaryEncoderListener listener;

    // based on [lastEncoded][encoded] lookup
    private static final int stateTable[][] = {
        {0, 1, 1, -1},
        {-1, 0, 1, -1},
        {-1, 1, 0, -1},
        {-1, 1, 1, 0}
    };
    
    private int stateA = 0;
    private int stateB = 0;

    private long lastUpdate = System.currentTimeMillis();
    
    private static final Logger LOGGER = Logger.getLogger(RotaryEncoder.class.getName());

    public RotaryEncoder(Pin pinA, Pin pinB, long initalValue) {

        encoderValue = initalValue;
        gpio = GpioFactory.getInstance();

        inputA = gpio.provisionDigitalInputPin(pinA, "PinA", com.pi4j.io.gpio.PinPullResistance.PULL_UP);
        inputB = gpio.provisionDigitalInputPin(pinB, "PinB", com.pi4j.io.gpio.PinPullResistance.PULL_UP);

        

        inputA.addListener(inputAListener);
//        inputB.addListener(inputBListener);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOGGER.info("RotarySwitch: Shutting down....");
                if (gpio != null) {
                    gpio.removeAllListeners();
                    gpio.shutdown();
                }
            }
        });
        LOGGER.log(Level.INFO, "RotarySwitch initialised on pinA {0} and pinB {1}",
                new String[]{pinA.getName(), pinB.getName()});
    }

    
    GpioPinListenerDigital inputAListener = new GpioPinListenerDigital() {

            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent gpdsce) {

                stateA = gpdsce.getState().getValue();
//                updateStates();
                int stateB = inputB.getState().getValue();
                calcEncoderValue(stateA, stateB);
                LOGGER.log(Level.FINE, "{0}{1} encodedValue: {2}", new Object[]{stateA, stateB, encoderValue});
            }
        };
    
    
    GpioPinListenerDigital inputBListener = new GpioPinListenerDigital() {

            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent gpdsce) {

                stateB = gpdsce.getState().getValue();
                updateStates();
//                int stateB = inputB.getState().getValue();
//                calcEncoderValue(stateA, stateB);
//                LOGGER.log(Level.FINE, "{0}{1} encodedValue: {2}", new Object[]{stateA, stateB, encoderValue});
            }
        };
    
    
    public long getValue() {
        return encoderValue;
    }

    public void setListener(RotaryEncoderListener listener) {
        this.listener = listener;
    }

    private void updateStates(){
        
        long time = System.currentTimeMillis();
        logger.info("state A: " + stateA);
        logger.info("state B: " + stateB);
        
//        int state = stateA ^ stateB;
        encoderValue ++;
//        logger.info("count impuls: " + encoderValue);
        
//        long millis = time - lastUpdate;
//        lastUpdate = time;
//        logger.info("delta time: " + millis);
        
        calcEncoderValue(stateA, stateB);
    }
    
    private void calcEncoderValue(int stateA, int stateB) {

       
        
        // converting the 2 pin value to single number to end up with 00, 01, 10 or 11
        int encoded = (stateA << 1) | stateB;

//        int encoded = stateA ^ stateB;
        logger.info("A:" + stateA+ " B:" + stateB + " Y:" + encoded);
        if (firstPass) {
            firstPass = false;
        } else {
            // going up states, 01, 11
            // going down states 00, 10
            int state = stateTable[lastEncoded][encoded];
            logger.info("encoded state: " + state);
            encoderValue += state;
            if (listener != null) {
                if (state == -1) {
                    listener.down(encoderValue);
                }
                if (state == 1) {
                    listener.up(encoderValue);
                }
            }
        }

        lastEncoded = encoded;
    }

    private static class PinPullResistance {

        public PinPullResistance() {
        }
    }

}
