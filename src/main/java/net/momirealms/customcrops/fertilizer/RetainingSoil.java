/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customcrops.fertilizer;

public class RetainingSoil implements Fertilizer{

    private double chance;
    private String key;
    private int times;
    private final boolean before;
    public String name;

    public RetainingSoil(String key, int times, double chance, boolean before){
        this.times = times;
        this.chance = chance;
        this.before = before;
        this.key = key;
    }

    @Override
    public String getKey() {return this.key;}
    @Override
    public int getTimes() {return this.times;}
    @Override
    public void setTimes(int times) {this.times = times;}
    @Override
    public boolean isBefore() {return this.before;}
    @Override
    public String getName() {return this.name;}

    public void setName(String name) {this.name = name;}
    public void setChance(double chance) {this.chance = chance;}
    public void setKey(String key) {this.key = key;}
    public double getChance() {return chance;}
}
