package com.libtowns.simulation;

import com.libtowns.GlobalConstants;
import com.libtowns.simulation.control.Command;
import com.libtowns.simulation.control.Message;
import com.libtowns.simulation.control.MessageRelevance;
import com.libtowns.simulation.systems.CellSystem;
import com.libtowns.simulation.systems.CommandPerformer;
import com.libtowns.simulation.systems.MarketSystem;
import com.libtowns.simulation.systems.NatureEventSystem;
import com.libtowns.simulation.systems.TownieSystem;
import com.libtowns.data.Map;
import com.libtowns.data.parts.Cell;
import java.util.Locale;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author rkriebel
 */
public class Core {

    private Map map = null;
    private boolean exit = false;
    private boolean running = false;
    private boolean paused = false;
    public Random random = new Random();
    private MarketSystem market_system;
    private int tick = 0;
    private GameRuntimeThread sim_thread;
    private Queue<Command> input = new ConcurrentLinkedQueue<Command>();
    private Queue<Message> output = new ConcurrentLinkedQueue<Message>();
    private TownieSystem townie_system;
    private CellSystem cell_system;
    private NatureEventSystem nature_event_system;
    private boolean jumping;

    private void proceedTick() {
        if (map != null) {
            this.tick++;
            map.setTick(tick);
            //todo player input Command react system here#!#
            while (!input.isEmpty()) {
                Command com = input.poll();
                if (com != null) {
                    CommandPerformer.proceed(map, com);
                }
            }

            townie_system.proceedTownies();
            cell_system.proceedCells();

            if (this.tick >= GlobalConstants.ticks_per_day) {
                this.map.newDay();
                this.tick = 0;
                //this.marketsystem.proceedDay();
                this.nature_event_system.proceedDay();
                this.cell_system.proceedDay();
                this.putMessage(MessageRelevance.DEBUG, "New Day: " + map.getDay());
                this.map.getStock().printStockStatus();
                if (!this.paused) {
                    System.gc();
                }
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Other minor methods">
    public int getTick() {
        return this.tick;
    }

    public void performCommand(Command com) {
        this.input.add(com);
    }

    public boolean start() {
        boolean ret = false;
        if (map != null) {
            if (this.paused) {
                this.paused = false;
                ret = true;
            } else {
                if (!running || exit) {
                    sim_thread = new GameRuntimeThread();
                    this.exit = false;
                    this.paused = false;
                    sim_thread.start();
                    Core.i().putMessage(MessageRelevance.DEBUG, "Start");
                    ret = true;
                } else {
                    ret = true;
                    Core.i().putMessage(MessageRelevance.DEBUG, "Already running");
                }
            }
        } else {
            Core.i().putMessage(MessageRelevance.DEBUG, "Map is missing");
        }
        return ret;
    }

    public void stop() {
        this.exit = true;
        this.paused = false;
        this.map.setTick(tick);
    }

    public void pause() {
        this.paused = true;
    }

    public void setMap(Map map) {
        this.map = map;
        this.tick = map.getTick();
        this.market_system = new MarketSystem(map.getMarketStock(), random);
        this.townie_system = new TownieSystem(map);
        this.cell_system = new CellSystem(map, random);
        this.nature_event_system = new NatureEventSystem(map, random);
    }

    public Map getMap() {
        return this.map;
    }

    public Map resset() {
        Map tmp_map = map;
        this.stop();
        this.map = null;
        return tmp_map;
    }

    public void destroy() {
        this.instance = null;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isExitet() {
        return exit;
    }

    private Core() {
        putMessage(MessageRelevance.DEBUG, "Your Language is set to: '" + Locale.getDefault().getLanguage() + "'.");
    }
    private static Core instance;

    public static Core i() {
        if (Core.instance == null) {
            Core.instance = new Core();
        }
        return instance;
    }

    public void jumpDays(float count) {

        pause();
        this.jumping = true;
        int ticks = (int) (count * GlobalConstants.ticks_per_day);
        for (int i = 0; i < ticks; i++) {
            Core.i().proceedTick();
        }

        System.gc();

        this.jumping = false;
        start();
    }

    public boolean isPaused() {
        return this.paused;
    }

    public boolean isJumping() {
        return jumping;
    }

    public void putMessage(Message message) {
        message.tick = this.getTimeTick();
        this.output.add(message);
    }

    public void putMessage(MessageRelevance type, String msg) {
        Message message = new Message(type, msg);
        message.tick = this.getTimeTick();
        this.output.add(message);
    }

    public void putMessage(MessageRelevance type, Cell cell, String msg) {
        Message message = new Message(type, cell, msg);
        message.tick = this.getTimeTick();
        this.output.add(message);
    }

    public Queue<Message> getOutput() {
        return this.output;
    }

    private long getTimeTick() {
        if (this.map != null) {
            return this.tick + map.getDay() * GlobalConstants.ticks_per_day;
        } else {
            return 0;
        }
    }

    // </editor-fold>
    private class GameRuntimeThread extends Thread {

        public GameRuntimeThread() {
            super(GameRuntimeThread.class.getSimpleName());
        }

        @Override
        public void run() {
            Core.i().putMessage(MessageRelevance.DEBUG, "Starting " + this.getName());
            long timestamp;
            long sleeptime;
            long duration;

            boolean[] slow = new boolean[100];
            byte point = 0;

            Core.i().running = true;
            do {
                timestamp = System.currentTimeMillis();

                if (!Core.i().paused) {
                    Core.i().proceedTick();
                }

                point++;
                if (point >= slow.length) {
                    point = 0;
                }

                duration = System.currentTimeMillis() - timestamp;
                sleeptime = GlobalConstants.tick_duration_millis - duration;
                //Core.i().putMessage(MessageRelevance.DEBUG,"DURATION: " + duration + " ms");
                if (sleeptime < 0) {
                    sleeptime = 0;

                    slow[point] = true;
                    byte count = 0;
                    for (int i = 0; i < slow.length; i++) {
                        if (slow[i]) {
                            count++;
                        }
                    }

                    if (count > 2) {
                        System.err.println("To slow computing! Simulation out of time ! "
                                + "\n\tTick duration was " + duration
                                + " ms and must be " + GlobalConstants.tick_duration_millis + " ms.");
                    }
                } else {
                    slow[point] = false;

                }
                //Core.i().putMessage(MessageType.DEBUG,"Tick runtime: " + duration);
                try {
                    sleep(sleeptime);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } while (!Core.i().exit);
            Core.i().putMessage(MessageRelevance.DEBUG, "End of " + this.getName());
            Core.i().running = false;
        }
    }
}
