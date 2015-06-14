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
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.text.DecimalFormat;
import java.text.Format;

/**
 *
 * @author dawid
 */
public class HC_SR04 {

    private final static Format DF22 = new DecimalFormat("#0.00");
    private final static double SOUND_SPEED = 34300;           // in cm, 343 m/s
    private final static double DIST_FACT = SOUND_SPEED / 2; // round trip
    private final static int MIN_DIST = 5;

    public HC_SR04()
            throws InterruptedException {
        System.out.println("GPIO Control - Range Sensor HC-SR04.");
        System.out.println("Will stop is distance is smaller than " + MIN_DIST + " cm");

        // create gpio controller 
        final GpioController gpio = GpioFactory.getInstance();

        final GpioPinDigitalOutput trigPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "Trig", PinState.LOW);
        final GpioPinDigitalInput echoPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, "Echo");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Oops!");
                gpio.shutdown();
                System.out.println("Exiting nicely.");
            }
        });

        System.out.println("Waiting for the sensor to be ready (2s)...");
        Thread.sleep(2000);

        boolean go = true;
        System.out.println("Looping until the distance is less than " + MIN_DIST + " cm");
        while (go) {
            System.out.println("loop");
            double start = 0d, end = 0d;
            trigPin.high();
      // 10 microsec to trigger the module  (8 ultrasound bursts at 40 kHz)  
            // https://www.dropbox.com/s/615w1321sg9epjj/hc-sr04-ultrasound-timing-diagram.png 
            try {
                Thread.sleep(0, 10000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            trigPin.low();

            // Wait for the signal to return 
            int i=100;
            while (echoPin.isLow()) {
                start = System.nanoTime();
                i--;
                
                if (i==0)
                    break;
            }
            
            i=100;
            // There it is 
            while (echoPin.isHigh()) {
                end = System.nanoTime();
                i--;
                if (i==0)
                    break;
            }

            if (end > 0 || start > 0) {
                double pulseDuration = (end - start) / 1000000000d; // in seconds
                
                System.out.println("end: " + end + " start: " + start +  "pulseDuration: " + pulseDuration);
                double distance = pulseDuration * DIST_FACT;
                if (distance < 1000) // Less than 10 meters
                {
                    System.out.println("Distance: " + DF22.format(distance) + " cm."); // + " (" + pulseDuration + " = " + end + " - " + start + ")");
                }//        if (distance > 0 && distance < MIN_DIST)
////          go = false;
//        else 
//        { 
//          if (distance < 0)
//            System.out.println("Dist:" + distance + ", start:" + start + ", end:" + end);
//          try { Thread.sleep(1000L); } catch (Exception ex) {}
//        } 
            }
            try {
                Thread.sleep(2000L);
            } catch (Exception ex) {
            }

        }
        System.out.println("Done.");
        trigPin.low(); // Off

        gpio.shutdown();
    }
}
