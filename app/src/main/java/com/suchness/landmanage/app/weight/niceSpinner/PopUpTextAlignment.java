package com.suchness.landmanage.app.weight.niceSpinner;

/**
 * @author: hejunfeng
 * @date: 2021/10/15 0015
 */
enum PopUpTextAlignment {
    START(0),
    END(1),
    CENTER(2);

    private final int id;

    PopUpTextAlignment(int id) {
        this.id = id;
    }

    static PopUpTextAlignment fromId(int id) {
        for (PopUpTextAlignment value : values()) {
            if (value.id == id) return value;
        }
        return CENTER;
    }
}
