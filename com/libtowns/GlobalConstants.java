package com.libtowns;

/**
 *
 * @author asuranis
 */
public class GlobalConstants {
    
    // good for townie animation speed
    public static final int tick_duration_millis = 125;
    // 1000/125  8 TPS (game updates per second) game framerate
    public static final int ticks_per_sec = (int) Math.ceil(1000/tick_duration_millis);
    
    //Default: 1 200 ticks/day   1 day in game = 2,5 min
    public static final int ticks_per_day = (150 * 1000) / tick_duration_millis;
            
    // townie moving speed 30 tiles /day
    public static byte cellsize = 40;
}
