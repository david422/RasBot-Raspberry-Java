/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dawid
 */
public class I2CUtil {
    
    //ED0 - battery voltage 0b1001000 0x48
    //ED2 -  left motor current 0b1001010 0x4A
    
    private final I2CBus i2CBus;
    private I2CDevice batteryVoltage;

    public I2CUtil() throws IOException {
        this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_1);
        
         batteryVoltage = i2CBus.getDevice(0x4A);
         
         new Thread(new Runnable() {

            @Override
            public void run() {
                while(true){
                    try {
                        
                        System.out.println("battery vlotage in int: " + batteryVoltage.read());
                        Thread.sleep(1000);
                    } catch (IOException ex) {
                        Logger.getLogger(I2CUtil.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(I2CUtil.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }
}
