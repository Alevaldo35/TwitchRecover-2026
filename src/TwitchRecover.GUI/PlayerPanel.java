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
 * Internal video player (embedded libVLC via vlcj) with a Twitch-like quality
 * selector and player options (play/pause, stop, ±10 s, volume, mute, seek).
 * Falls back to launching VLC if libVLC is not available.
 */
class PlayerPanel extends JPanel {
    private final Runnable onBack;
    private EmbeddedMediaPlayerComponent media;
    private boolean available;

    private final JLabel titleLabel = new JLabel();
    private final JComboBox<String> qualityCombo = new JComboBox<String>();
    private final JButton playPause;
    private final JButton stopBtn;
    private final JButton muteBtn;
    private final JSlider seek;
    private final JSlider volume;
    private final JLabel timeLabel = new JLabel("0:00 / 0:00");
    private final Timer timer;

    private List<String> urls;
    private String currentUrl;
    private boolean seeking = false;
    private boolean updatingCombo = false;

    PlayerPanel(Runnable onBack) {
        this.onBack = onBack;
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        // ---- Top bar: back, title, quality ----
        JButton back = Ui.subtleButton(I18n.t("player.back"));
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
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(0x14, 0x14, 0x16));
        top.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        topLeft.setOpaque(false);
        topLeft.add(back);
        topLeft.add(titleLabel);
        top.add(topLeft, BorderLayout.WEST);
        JButton openVlc = darkButton(I18n.t("player.openVlc"));
        openVlc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { if (currentUrl != null) Player.play(currentUrl); }
        });
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topRight.setOpaque(false);
        topRight.add(openVlc);
        topRight.add(qualityCombo);
        top.add(topRight, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // ---- Center: video surface (or fallback) ----
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

        // ---- Bottom: full control bar ----
        playPause = darkButton(I18n.t("btn.pause"));
        playPause.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { togglePlay(); }
        });
        stopBtn = darkButton(I18n.t("btn.stop"));
        stopBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { if (available && media != null) media.mediaPlayer().controls().stop(); }
        });
        JButton back10 = darkButton(I18n.t("btn.back10"));
        back10.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { if (available && media != null) media.mediaPlayer().controls().skipTime(-10000); }
        });
        JButton fwd10 = darkButton(I18n.t("btn.fwd10"));
        fwd10.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { if (available && media != null) media.mediaPlayer().controls().skipTime(10000); }
        });
        muteBtn = darkButton(I18n.t("btn.mute"));
        muteBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (available && media != null) {
                    media.mediaPlayer().audio().mute();
                    muteBtn.setText(I18n.t(media.mediaPlayer().audio().isMute() ? "btn.unmute" : "btn.mute"));
                }
            }
        });
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setFont(Ui.font(12, Font.PLAIN));

        seek = new JSlider(0, 1000, 0);
        seek.setOpaque(false);
        seek.addChangeListener(e -> {
            if (seek.getValueIsAdjusting() && available && media != null) {
                seeking = true;
                media.mediaPlayer().controls().setPosition(seek.getValue() / 1000f);
            } else {
                seeking = false;
            }
        });
        volume = new JSlider(0, 100, 90);
        volume.setOpaque(false);
        volume.setPreferredSize(new Dimension(110, 22));
        volume.addChangeListener(e -> {
            if (available && media != null) media.mediaPlayer().audio().setVolume(volume.getValue());
        });
        JLabel volLabel = new JLabel(I18n.t("player.volume"));
        volLabel.setForeground(new Color(0xBB, 0xBB, 0xC2));
        volLabel.setFont(Ui.font(11, Font.PLAIN));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setOpaque(false);
        controls.add(playPause);
        controls.add(stopBtn);
        controls.add(back10);
        controls.add(fwd10);
        controls.add(timeLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(muteBtn);
        right.add(volLabel);
        right.add(volume);

        JPanel barTop = new JPanel(new BorderLayout(12, 0));
        barTop.setOpaque(false);
        barTop.add(controls, BorderLayout.WEST);
        barTop.add(right, BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setBackground(new Color(0x14, 0x14, 0x16));
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 14, 12, 14));
        bottom.add(seek, BorderLayout.NORTH);
        bottom.add(barTop, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        timer = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) { tick(); }
        });
    }

    private JButton darkButton(String text) {
        JButton b = new JButton(text);
        b.setFont(Ui.font(12, Font.BOLD));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
                "arc: 999; borderWidth: 0; focusWidth: 0; background: #2A2A30; foreground: #FFFFFF");
        b.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
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
            Player.play(url); // Fallback to external VLC.
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
