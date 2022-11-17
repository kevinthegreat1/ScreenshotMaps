package com.kevinthegreat.screenshotmaps.util;

import java.util.Iterator;

public class Grid implements Iterable<Point>, Iterator<Point> {
    public final int x;
    public final int y;
    public final int dx;
    public final int dy;
    public final int xCount;
    public final int yCount;
    private int xIndex;
    private int yIndex;

    public Grid(int x, int y, int dx, int dy, int xCount, int yCount) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.xCount = xCount;
        this.yCount = yCount;
    }

    public Grid(Triple<Integer, Integer, Integer> x, Triple<Integer, Integer, Integer> y) {
        this(x.a(), y.a(), x.b(), y.b(), x.c(), y.c());
    }

    @Override
    public Iterator<Point> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return yIndex < yCount;
    }

    @Override
    public Point next() {
        Point point = new Point(x + xIndex++ * dx, y + yIndex * dy);
        if (xIndex >= xCount) {
            xIndex = 0;
            yIndex++;
        }
        return point;
    }
}
