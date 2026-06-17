/*
 * Copyright (c) 2020, 2021 Daylam Tayari <daylam@tayari.gg>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *  Original project by Daylam Tayari - https://github.com/TwitchRecover/TwitchRecover
 *  Clean GUI added in this fork by Alevaldo35 - https://github.com/Alevaldo35
 */

package TwitchRecover.GUI;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Small theme + factory helpers to keep the look clean, flat and consistent.
 */
final class Ui {
    private Ui() {}

    // Palette (Twitch purple accent over an Apple-like light grey canvas).
    static final Color ACCENT          = new Color(0x91, 0x46, 0xFF);
    static final Color ACCENT_HOVER    = new Color(0x7D, 0x2E, 0xF0);
    static final Color CANVAS          = new Color(0xFF, 0xFF, 0xFF);
    static final Color SIDEBAR         = new Color(0xF5, 0xF5, 0xF7);
    static final Color CARD            = new Color(0xFB, 0xFB, 0xFD);
    static final Color TEXT            = new Color(0x1D, 0x1D, 0x1F);
    static final Color TEXT_SECONDARY  = new Color(0x6E, 0x6E, 0x73);
    static final Color BORDER          = new Color(0xE3, 0xE3, 0xE8);
    static final Color SELECTED_BG     = new Color(0xEC, 0xE5, 0xFF);

    static Font font(int size, int style) {
        return new Font("Segoe UI", style, size);
    }

    static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(26, Font.BOLD));
        l.setForeground(TEXT);
        return l;
    }

    static JLabel subtitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(14, Font.PLAIN));
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(12, Font.BOLD));
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    static JTextField field(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(font(14, Font.PLAIN));
        f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        f.putClientProperty(FlatClientProperties.STYLE, "arc: 14");
        f.setBorder(BorderFactory.createCompoundBorder(
                f.getBorder(),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return f;
    }

    static JButton primaryButton(String text) {
        final JButton b = new JButton(text);
        b.setFont(font(14, Font.BOLD));
        b.setForeground(Color.WHITE);
        b.setBackground(ACCENT);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty(FlatClientProperties.STYLE,
                "arc: 999; borderWidth: 0; focusWidth: 0; innerFocusWidth: 0");
        b.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (b.isEnabled()) b.setBackground(ACCENT_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                if (b.isEnabled()) b.setBackground(ACCENT);
            }
        });
        return b;
    }

    static JButton subtleButton(String text) {
        JButton b = new JButton(text);
        b.setFont(font(13, Font.PLAIN));
        b.setForeground(TEXT);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty(FlatClientProperties.STYLE,
                "arc: 999; borderColor: " + hex(BORDER) + "; background: " + hex(Color.WHITE));
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return b;
    }

    static String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
