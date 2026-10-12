package com.videomenu;

/** Shared measurements keep controls, pagination, status and footer in separate rows. */
public record AstralLayout(int left, int right, int colWidth, int headerY, int top,
                           int step, int rows, int pagerY, int statusY, int footerY, boolean singleColumn) {
    public static AstralLayout of(int width, int height) {
        boolean single = width < 320;
        int content = Math.max(24, Math.min(single ? 480 : 640, width-24));
        int column = single ? content : (content-12)/2;
        int left = (width-content)/2;
        int top = single ? 76 : height < 210 ? 52 : 62;
        int step = height < 280 ? 18 : 24;
        int pager = height-72;
        int rows = Math.max(1,(pager-4-top)/step);
        return new AstralLayout(left, single ? left : left+column+12, column,
            top-13,top,step,rows,pager,height-50,height-30,single);
    }
}
