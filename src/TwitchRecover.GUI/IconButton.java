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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;

/** A clean, minimal vector-drawn icon button (no text/emoji), white on dark. */
class IconButton extends JButton {
    enum Kind { PLAY, PAUSE, REPLAY10, FORWARD10, VOLUME, MUTE, FULLSCREEN, EXIT_FULLSCREEN, BACK, EXTERNAL }

    private Kind kind;
    private boolean hover;
    private final int size;

    IconButton(Kind kind, int size) {
        this.kind = kind;
        this.size = size;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { hover = true; repaint(); }
            public void mouseExited(java.awt.event.MouseEvent e) { hover = false; repaint(); }
        });
    }

    void setKind(Kind k) { this.kind = k; repaint(); }

    public Dimension getPreferredSize() { return new Dimension(size + 16, size + 16); }
    public Dimension getMaximumSize() { return getPreferredSize(); }
    public Dimension getMinimumSize() { return getPreferredSize(); }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int w = getWidth(), h = getHeight();
        if (hover) {
            g2.setColor(new Color(255, 255, 255, 28));
            int d = Math.min(w, h);
            g2.fillOval((w - d) / 2, (h - d) / 2, d, d);
        }

        int s = size;
        int x = (w - s) / 2, y = (h - s) / 2;
        Color c = hover ? Color.WHITE : new Color(0xEC, 0xEC, 0xF0);
        g2.setColor(c);
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (kind) {
            case PLAY: {
                GeneralPath p = new GeneralPath();
                p.moveTo(x + s * 0.18, y + s * 0.12);
                p.lineTo(x + s * 0.18, y + s * 0.88);
                p.lineTo(x + s * 0.86, y + s * 0.50);
                p.closePath();
                g2.fill(p);
                break;
            }
            case PAUSE: {
                int bw = (int) (s * 0.20), gap = (int) (s * 0.18);
                g2.fillRoundRect((int) (x + s * 0.22), y + 2, bw, s - 4, 4, 4);
                g2.fillRoundRect((int) (x + s * 0.22 + bw + gap), y + 2, bw, s - 4, 4, 4);
                break;
            }
            case REPLAY10: drawSeek(g2, x, y, s, true); break;
            case FORWARD10: drawSeek(g2, x, y, s, false); break;
            case VOLUME: drawVolume(g2, x, y, s, false); break;
            case MUTE: drawVolume(g2, x, y, s, true); break;
            case FULLSCREEN: drawCorners(g2, x, y, s, true); break;
            case EXIT_FULLSCREEN: drawCorners(g2, x, y, s, false); break;
            case BACK: {
                g2.drawLine((int) (x + s * 0.62), (int) (y + s * 0.18), (int) (x + s * 0.30), (int) (y + s * 0.50));
                g2.drawLine((int) (x + s * 0.30), (int) (y + s * 0.50), (int) (x + s * 0.62), (int) (y + s * 0.82));
                g2.drawLine((int) (x + s * 0.30), (int) (y + s * 0.50), (int) (x + s * 0.86), (int) (y + s * 0.50));
                break;
            }
            case EXTERNAL: {
                int bx = (int) (x + s * 0.12), by = (int) (y + s * 0.28);
                g2.drawRoundRect(bx, by, (int) (s * 0.55), (int) (s * 0.6), 5, 5);
                g2.drawLine((int) (x + s * 0.55), (int) (y + s * 0.45), (int) (x + s * 0.9), (int) (y + s * 0.12));
                g2.drawLine((int) (x + s * 0.66), (int) (y + s * 0.12), (int) (x + s * 0.9), (int) (y + s * 0.12));
                g2.drawLine((int) (x + s * 0.9), (int) (y + s * 0.12), (int) (x + s * 0.9), (int) (y + s * 0.36));
                break;
            }
        }
        g2.dispose();
    }

    private void drawSeek(Graphics2D g2, int x, int y, int s, boolean back) {
        // Circular arrow + "10".
        Arc2D arc = new Arc2D.Double(x + s * 0.12, y + s * 0.12, s * 0.76, s * 0.76,
                back ? 70 : 110, back ? 250 : -250, Arc2D.OPEN);
        g2.draw(arc);
        // Arrowhead at the arc start.
        double ax = back ? x + s * 0.30 : x + s * 0.70;
        double ay = y + s * 0.16;
        GeneralPath head = new GeneralPath();
        if (back) {
            head.moveTo(ax, ay - s * 0.04);
            head.lineTo(ax - s * 0.12, ay);
            head.lineTo(ax, ay + s * 0.14);
        } else {
            head.moveTo(ax, ay - s * 0.04);
            head.lineTo(ax + s * 0.12, ay);
            head.lineTo(ax, ay + s * 0.14);
        }
        head.closePath();
        g2.fill(head);
        // "10"
        g2.setFont(Ui.font((int) (s * 0.34), Font.BOLD));
        FontMetrics fm = g2.getFontMetrics();
        String t = "10";
        g2.drawString(t, x + (s - fm.stringWidth(t)) / 2f, y + s * 0.62f);
    }

    private void drawVolume(Graphics2D g2, int x, int y, int s, boolean muted) {
        GeneralPath sp = new GeneralPath();
        sp.moveTo(x + s * 0.14, y + s * 0.38);
        sp.lineTo(x + s * 0.30, y + s * 0.38);
        sp.lineTo(x + s * 0.48, y + s * 0.20);
        sp.lineTo(x + s * 0.48, y + s * 0.80);
        sp.lineTo(x + s * 0.30, y + s * 0.62);
        sp.lineTo(x + s * 0.14, y + s * 0.62);
        sp.closePath();
        g2.fill(sp);
        if (muted) {
            g2.drawLine((int) (x + s * 0.60), (int) (y + s * 0.36), (int) (x + s * 0.84), (int) (y + s * 0.64));
            g2.drawLine((int) (x + s * 0.84), (int) (y + s * 0.36), (int) (x + s * 0.60), (int) (y + s * 0.64));
        } else {
            g2.draw(new Arc2D.Double(x + s * 0.34, y + s * 0.30, s * 0.36, s * 0.40, -55, 110, Arc2D.OPEN));
            g2.draw(new Arc2D.Double(x + s * 0.30, y + s * 0.20, s * 0.56, s * 0.60, -55, 110, Arc2D.OPEN));
        }
    }

    private void drawCorners(Graphics2D g2, int x, int y, int s, boolean expand) {
        int a = (int) (s * 0.16), b = (int) (s * 0.40);
        if (expand) {
            // top-left
            g2.drawLine(x, y + a, x, y); g2.drawLine(x, y, x + a, y);
            // top-right
            g2.drawLine(x + s - a, y, x + s, y); g2.drawLine(x + s, y, x + s, y + a);
            // bottom-left
            g2.drawLine(x, y + s - a, x, y + s); g2.drawLine(x, y + s, x + a, y + s);
            // bottom-right
            g2.drawLine(x + s - a, y + s, x + s, y + s); g2.drawLine(x + s, y + s, x + s, y + s - a);
        } else {
            g2.drawLine(x + b - a, y, x + b, y); g2.drawLine(x + b, y, x + b, y + a);
            g2.drawLine(x + s - b, y, x + s - b, y + a); g2.drawLine(x + s - b, y + a, x + s - b + a, y + a);
            g2.drawLine(x + b, y + s - a, x + b, y + s); g2.drawLine(x + b - a, y + s, x + b, y + s);
            g2.drawLine(x + s - b, y + s - a, x + s - b, y + s); g2.drawLine(x + s - b, y + s - a, x + s - b + a, y + s - a);
        }
    }
}
