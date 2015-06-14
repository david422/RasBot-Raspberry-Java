/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.impl.PinImpl;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dawid
 */
public class DistanceSensor {

    private static final Logger logger = Logger.getLogger("DistanceSensor");

    private static final int TRIG_5 = 7;
    private static final int ECHO_5 = 0;

    private static final int TRIG_4 = 2;
    private static final int ECHO_4 = 3;

    private static final int TRIG_3 = 12;
    private static final int ECHO_3 = 13;

    private static final int TRIG_2 = 14;
    private static final int ECHO_2 = 12;

    private static final int TRIG_1 = 22;
    private static final int ECHO_1 = 23;

    private GpioPinDigitalOutput trigPin;
    private GpioPinDigitalInput echoPin;

    long startTime = System.currentTimeMillis();
    long endTime = System.currentTimeMillis();

    public DistanceSensor(int whichSensor) {

        GpioController gpioFactory = GpioFactory.getInstance();
        switch (whichSensor) {
            case 5:
                trigPin = gpioFactory.provisionDigitalOutputPin(RaspiPin.GPIO_07, "TRIG_1", PinState.LOW);
                echoPin = gpioFactory.provisionDigitalInputPin(RaspiPin.GPIO_00, "ECHO_1");
                break;

        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                
                trigPin.high();
                
                try {
                    Thread.sleep(10000, 10000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DistanceSensor.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                trigPin.low();
                
                while(echoPin.isLow())
                    startTime = System.nanoTime();
                
                while(echoPin.isHigh())
                    endTime = System.nanoTime();
                
                if (endTime>0 && startTime>0){
                    double pulseDuartion = (endTime - startTime)/1000000000d;
                    double distance = pulseDuartion * 17150;
                    
                     System.out.println("Distance: " + distance + " cm.");
                }
            }
        }).start();

    }

}
