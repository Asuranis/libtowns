package com.libtowns.data;

import com.libtowns.data.parts.Cell;
import com.libtowns.data.parts.ResourceType;
import com.libtowns.data.parts.Townie;
import com.libtowns.data.parts.CellClass;
import com.libtowns.data.parts.CellType;
import com.libtowns.data.parts.Stock;
import com.libtowns.data.parts.StockSlot;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by robin on 23.10.15.
 *
 *
 */
public class Map implements Serializable {

    public final int area_size;
    public final byte size;
    public final long seed;
    private Cell[][] grid;
    private Stock stock = new Stock();
    private Cell castle_cell;
    private Cell market_cell;
    private Stock market_stock = new Stock();
    private List<Townie> ltownie = new ArrayList();
    private long day = 0;
    private int tick = 0;
    private long latest_nature_event = 0;

    public Map(int size, long seed) {

        this.seed = seed;
        if (size < 6) {
            size = 6;
        }

        if (size > Byte.MAX_VALUE) {
            size = Byte.MAX_VALUE;
        }

        this.size = (byte) size;

        this.area_size = this.size * this.size;

        grid = new Cell[this.size][this.size];


        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                grid[i][j] = new Cell(i, j);
                grid[i][j].creationComplete();
            }
        }

        setCell(this.size / 2, this.size / 2, CellType.CASTLE, 0);
        Cell cell = getCell(this.size / 2, this.size / 2);
        cell.creationComplete();
        setCastle(cell);
    }

    public Cell getCell(int pos_x, int pos_y) {
        if (pos_x >= 0 && pos_y >= 0 && pos_x < this.size && pos_y < this.size) {
            return this.grid[pos_x][pos_y];
        } else {
            return null;
        }
    }

    public void setCell(int pos_x, int pos_y, CellType type) {
        Cell cell = this.getCell(pos_x, pos_y);
        if (cell != null) {
            cell.recreate(type);
        }
    }

    public void setCell(int pos_x, int pos_y, CellType type, int subType) {
        Cell cell = this.getCell(pos_x, pos_y);
        if (cell != null) {
            cell.recreate(type, subType);
        }
    }

    public long getDay() {
        return day;
    }

    public long getLatest_nature_event() {
        return latest_nature_event;
    }

    public Stock getStock() {
        if (this.castle_cell != null) {
            stock.setLevel(this.castle_cell.getLevel());
        }
        return stock;
    }

    public List<Townie> getLtownie() {
        return ltownie;
    }

    public Stock getMarketStock() {
        if (this.market_cell != null) {
            market_stock.setLevel(this.market_cell.getLevel());
        }
        return market_stock;
    }

    public void printTileTypeGrid() {

        for (int j = 0; j < this.size; j++) {

            for (int i = 0; i < this.size; i++) {
                System.out.print(this.grid[i][j].getTypeID() + ":" + this.grid[i][j].getSubTypeID() + " ");
                if (this.grid[i][j].getTypeID() != 0 && this.grid[i][j].getSubTypeID() > 2) {
                    System.err.println("Tile error");
                }
            }
            System.out.println();
        }
    }

    public void printGrid() {
        String znak;
        for (int j = 0; j < this.size; j++) {

            for (int i = 0; i < this.size; i++) {
                switch (CellType.getByID(this.grid[i][j].getTypeID())) {
                    case PLAINS:
                        znak = "...";//∞∞
                        break;
                    case FOREST:
                        znak = "$$$";
                        break;
                    case POND:
                        znak = " O ";
                        break;
                    case FIELD:
                        znak = "|||";
                        break;
                    case RIVER:
                        znak = " ~ ";
                        break;
                    case SWAMP:
                        znak = " ¤ ";
                        break;
                    case ORE_MOUNT:
                        znak = "[@]";
                        break;
                    case QUARRY:
                        znak = "[#]";
                        break;
                    case CASTLE:
                        znak = "]^[";
                        break;
                    default:
                        znak = " ! ";
                        break;
                }
                System.out.print(znak);

            }
            System.out.println();
        }
    }

    public void printRiverGrid() {
        String znak;
        for (int j = 0; j < this.size; j++) {

            for (int i = 0; i < this.size; i++) {
                switch (CellType.getByID(this.grid[i][j].getTypeID())) {
                    case RIVER:
                        switch (this.grid[i][j].getSubTypeID()) {
                            case 0:
                                znak = "═";
                                break;
                            case 1:
                                znak = "║";
                                break;
                            case 2:
                                znak = "╗";
                                break;
                            case 3:
                                znak = "╝";
                                break;
                            case 4:
                                znak = "╚";
                                break;
                            case 5:
                                znak = "╔";
                                break;
                            default:
                                znak = "Err";
                                break;
                        }

                        break;
                    default:
                        znak = "░";
                        break;
                }
                System.out.print(znak);

            }
            System.out.println();
        }
    }

    public void printGridTownies() {
        String znak;
        for (int j = 0; j < this.size; j++) {

            for (int i = 0; i < this.size; i++) {
                znak = "";
                for (Townie tow : this.ltownie) {
                    if (tow.getPosX() == i && tow.getPosY() == j) {
                        znak = " ☺ ";
                    }
                }
                if (znak.length() < 3) {
                    znak = " , ";
                }

                System.out.print(znak);

            }
            System.out.println();
        }
    }

    public Cell getRandomCell(CellType type) {
        Cell cell = null;
        int count = 0;
        Random rn = new Random();

        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                if (grid[i][j].getTypeID() == type.getCellID()) {
                    count++;
                }
            }
        }

        if (count > 0) {
            do {
                cell = this.getCell(rn.nextInt(this.size), rn.nextInt(this.size));
                if (cell.getTypeID() == type.getCellID()) {
                    return cell;
                }
            } while (count > 0);

        }

        return cell;
    }

    public void setLatest_nature_event(long day) {
        this.latest_nature_event = day;
    }

    public void newDay() {
        this.day++;
    }

    public Cell getRandomBuilding() {
        Cell cell = null;
        int count = 0;
        Random rn = new Random();

        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                if (grid[i][j].getCellClass() != CellClass.NATURE && grid[i][j].getCellClass() != CellClass.SOURCE && CellType.getByID(grid[i][j].getTypeID()) != CellType.CASTLE) {
                    count++;
                }
            }
        }

        if (count > 0) {
            do {
                cell = this.getCell(rn.nextInt(this.size), rn.nextInt(this.size));
            } while (cell.getCellClass() != CellClass.NATURE && cell.getCellClass() != CellClass.SOURCE && CellType.getByID(cell.getTypeID()) == CellType.CASTLE);

        }

        return cell;
    }

    public Cell getCastle() {
        return castle_cell;
    }

    public void setCastle(Cell tile) {
        this.castle_cell = tile;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public StockSlot[] getMapResourcesSum() {
        StockSlot[] ret = new StockSlot[ResourceType.getResourceTypes().length];
        for (int j = 0; j < this.size; j++) {
            for (int i = 0; i < this.size; i++) {
            }
        }

        for (ResourceType res : ResourceType.getResourceTypes()) {
            System.out.println(res);
        }
        return ret;
    }
}
