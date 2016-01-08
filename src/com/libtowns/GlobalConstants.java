package com.libtowns;

/**
 *
 * @author rkriebel
 */
public class GlobalConstants {
    
    // good for townie animation speed
    public static final int tick_duration_millis = 120;
    // 1000/120  8 GUPS (game updates per second) game framerate
    public static final int default_gups = (int) Math.ceil(1000/tick_duration_millis);
    
    //Default: 1 200 ticks/day   1 day in game = 2,4 min => 144 sec realtime
    public static final int ticks_per_day = (144 * 1000) / tick_duration_millis;
            
    // townie moving speed 30 tiles /day
    public static byte cellsize = 40;
}
