/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author dawid
 */
public class NativeUtils {
    
    public static native void initializeLeftMotorPd();

    public static native void initializeRightMotorPd();

    public static native void setLeftMotorRPM(int rpm);

    public static native void setRightMotorRPM(int rpm);
    
    static{
        System.loadLibrary("JNIRasbot");
    }
}
