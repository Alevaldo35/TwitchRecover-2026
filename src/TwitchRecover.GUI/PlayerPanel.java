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

import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Internal video player (embedded libVLC) with a sleek, dark, Netflix-like
 * control bar: transport, seek, volume, fullscreen and a system-player fallback.
 */
class PlayerPanel extends JPanel {
    private static final Color BAR = new Color(0x12, 0x12, 0x16);
    private static final Color BTN = new Color(0x2A, 0x2A, 0x32);

    private final Runnable onBack;
    private final Runnable onFullscreen;
    private EmbeddedMediaPlayerComponent media;
    private boolean available;

    private final JLabel titleLabel = new JLabel();
    private final JComboBox<String> qualityCombo = new JComboBox<String>();
    private final JButton playPause;
    private final JButton muteBtn;
    private final JSlider seek;
    private final JSlider volume;
    private final JLabel timeLabel = new JLabel("0:00 / 0:00");
    private final Timer timer;
    private JPanel topBar;
    private JPanel bottomBar;
    private Timer hideTimer;
    private boolean immersive = false;
    private Cursor blankCursor;

    private List<String> urls;
    private String currentUrl;
    private boolean seeking = false;
    private boolean updatingCombo = false;

    PlayerPanel(Runnable onBack, Runnable onFullscreen) {
        this.onBack = onBack;
        this.onFullscreen = onFullscreen;
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        // ---- Top bar ----
        JButton back = pill(I18n.t("player.back"), false);
        back.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { stop(); if (onBack != null) onBack.run(); }
        });
        titleLabel.setFont(Ui.font(14, Font.BOLD));
        titleLabel.setForeground(Color.WHITE);
        qualityCombo.setFont(Ui.font(12, Font.PLAIN));
        qualityCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (updatingCombo) return;
                int i = qualityCombo.getSelectedIndex();
                if (urls != null && i >= 0 && i < urls.size()) play(urls.get(i));
            }
        });
        JButton ext = pill(I18n.t("player.external"), false);
        ext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { if (currentUrl != null) Player.play(currentUrl); }
        });

        JPanel top = new JPanel(new BorderLayout());
        topBar = top;
        top.setBackground(BAR);
        top.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        topLeft.setOpaque(false);
        topLeft.add(back);
        topLeft.add(titleLabel);
        top.add(topLeft, BorderLayout.WEST);
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topRight.setOpaque(false);
        topRight.add(ext);
        topRight.add(qualityCombo);
        top.add(topRight, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // ---- Center: video (or fallback) ----
        JComponent center;
        try {
            media = new EmbeddedMediaPlayerComponent();
            available = true;
            center = media;
        } catch (Throwable t) {
            available = false;
            JPanel fb = new JPanel(new GridBagLayout());
            fb.setBackground(Color.BLACK);
            JLabel l = new JLabel(I18n.t("player.unavailable"));
            l.setForeground(Color.WHITE);
            l.setFont(Ui.font(14, Font.PLAIN));
            fb.add(l);
            center = fb;
        }
        add(center, BorderLayout.CENTER);

        // ---- Bottom: seek + controls ----
        seek = new JSlider(0, 1000, 0);
        seek.setOpaque(false);
        seek.setForeground(Ui.ACCENT);
        seek.addChangeListener(e -> {
            if (seek.getValueIsAdjusting() && available && media != null) {
                seeking = true;
                media.mediaPlayer().controls().setPosition(seek.getValue() / 1000f);
            } else {
                seeking = false;
            }
        });

        JButton back10 = pill("− 10 s", false);
        back10.addActionListener(skip(-10000));
        JButton fwd10 = pill("+ 10 s", false);
        fwd10.addActionListener(skip(10000));
        playPause = pill(I18n.t("btn.pause"), true);
        playPause.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { togglePlay(); }
        });
        JButton stopBtn = pill(I18n.t("btn.stop"), false);
        stopBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { if (available && media != null) media.mediaPlayer().controls().stop(); }
        });

        JPanel transport = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        transport.setOpaque(false);
        transport.add(back10);
        transport.add(playPause);
        transport.add(fwd10);
        transport.add(stopBtn);

        timeLabel.setForeground(Color.WHITE);
        timeLabel.setFont(Ui.font(12, Font.PLAIN));
        muteBtn = pill(I18n.t("btn.mute"), false);
        muteBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (available && media != null) {
                    media.mediaPlayer().audio().mute();
                    muteBtn.setText(I18n.t(media.mediaPlayer().audio().isMute() ? "btn.unmute" : "btn.mute"));
                }
            }
        });
        volume = new JSlider(0, 100, 90);
        volume.setOpaque(false);
        volume.setPreferredSize(new Dimension(100, 22));
        volume.addChangeListener(e -> {
            if (available && media != null) media.mediaPlayer().audio().setVolume(volume.getValue());
        });
        JButton fs = pill(I18n.t("btn.fullscreen"), false);
        fs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { if (onFullscreen != null) onFullscreen.run(); }
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(timeLabel);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(muteBtn);
        right.add(volume);
        right.add(fs);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(left, BorderLayout.WEST);
        row.add(transport, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottomBar = bottom;
        bottom.setBackground(BAR);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 16, 12, 16));
        bottom.add(seek, BorderLayout.NORTH);
        bottom.add(row, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        timer = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) { tick(); }
        });

        // Auto-hide controls in fullscreen (YouTube/Netflix style).
        blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB),
                new Point(0, 0), "blank");
        hideTimer = new Timer(2800, new ActionListener() {
            public void actionPerformed(ActionEvent e) { if (immersive) setControlsVisible(false); }
        });
        hideTimer.setRepeats(false);
        java.awt.event.MouseAdapter wake = new java.awt.event.MouseAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent e) { onActivity(); }
            public void mouseDragged(java.awt.event.MouseEvent e) { onActivity(); }
            public void mousePressed(java.awt.event.MouseEvent e) { onActivity(); }
        };
        addMouseMotionListener(wake);
        addMouseListener(wake);
        if (available && media != null) {
            try {
                java.awt.Component vs = media.videoSurfaceComponent();
                if (vs != null) { vs.addMouseMotionListener(wake); vs.addMouseListener(wake); }
            } catch (Exception ignored) {}
        }
    }

    /** Enable/disable immersive (fullscreen) auto-hide of the controls. */
    void setImmersive(boolean on) {
        immersive = on;
        setControlsVisible(true);
        if (on) hideTimer.restart();
        else hideTimer.stop();
    }

    private void onActivity() {
        if (!immersive) return;
        setControlsVisible(true);
        hideTimer.restart();
    }

    private void setControlsVisible(boolean visible) {
        if (topBar != null) topBar.setVisible(visible);
        if (bottomBar != null) bottomBar.setVisible(visible);
        Cursor c = visible ? Cursor.getDefaultCursor() : blankCursor;
        setCursor(c);
        if (available && media != null) {
            try {
                java.awt.Component vs = media.videoSurfaceComponent();
                if (vs != null) vs.setCursor(c);
            } catch (Exception ignored) {}
        }
        revalidate();
        repaint();
    }

    private ActionListener skip(final int ms) {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (available && media != null) media.mediaPlayer().controls().skipTime(ms);
            }
        };
    }

    /** A sleek dark rounded button; primary=accent for the main action. */
    private JButton pill(String text, boolean primary) {
        final JButton b = new JButton(text);
        b.setFont(Ui.font(13, Font.BOLD));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        final Color base = primary ? Ui.ACCENT : BTN;
        final Color hover = primary ? Ui.ACCENT_HOVER : new Color(0x3A, 0x3A, 0x44);
        b.setBackground(base);
        b.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
                "arc: 999; borderWidth: 0; focusWidth: 0; innerFocusWidth: 0");
        b.setBorder(BorderFactory.createEmptyBorder(primary ? 9 : 7, primary ? 22 : 16, primary ? 9 : 7, primary ? 22 : 16));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(base); }
        });
        return b;
    }

    /** Open the player with a list of qualities; starts at startIndex. */
    void open(String title, List<String> labels, List<String> urls, int startIndex) {
        this.urls = urls;
        titleLabel.setText(title == null ? "" : title);
        updatingCombo = true;
        qualityCombo.removeAllItems();
        for (String l : labels) qualityCombo.addItem(l);
        if (startIndex >= 0 && startIndex < labels.size()) qualityCombo.setSelectedIndex(startIndex);
        updatingCombo = false;
        int i = qualityCombo.getSelectedIndex();
        if (urls != null && i >= 0 && i < urls.size()) play(urls.get(i));
        timer.start();
    }

    private void play(String url) {
        currentUrl = url;
        playPause.setText(I18n.t("btn.pause"));
        if (available && media != null) {
            media.mediaPlayer().media().play(url);
            media.mediaPlayer().audio().setVolume(volume.getValue());
        } else {
            Player.play(url);
        }
    }

    private void togglePlay() {
        if (!available || media == null) return;
        media.mediaPlayer().controls().pause();
        boolean playing = media.mediaPlayer().status().isPlaying();
        playPause.setText(I18n.t(playing ? "btn.pause" : "btn.play"));
    }

    private void tick() {
        if (!available || media == null) return;
        if (!seeking) {
            float pos = media.mediaPlayer().status().position();
            if (pos >= 0) seek.setValue((int) (pos * 1000));
        }
        long t = media.mediaPlayer().status().time();
        long len = media.mediaPlayer().status().length();
        timeLabel.setText(fmt(t) + " / " + fmt(len));
    }

    private static String fmt(long ms) {
        if (ms < 0) ms = 0;
        long s = ms / 1000;
        long h = s / 3600, m = (s % 3600) / 60, sec = s % 60;
        if (h > 0) return String.format("%d:%02d:%02d", h, m, sec);
        return String.format("%d:%02d", m, sec);
    }

    void stop() {
        timer.stop();
        if (available && media != null) {
            try { media.mediaPlayer().controls().stop(); } catch (Exception ignored) {}
        }
    }
}
