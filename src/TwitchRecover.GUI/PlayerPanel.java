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
 * Internal video player (embedded libVLC) with a clean, minimal, icon-based
 * control bar (play, ±10 s, volume, fullscreen) that auto-hides in fullscreen.
 */
class PlayerPanel extends JPanel {
    private static final Color BAR = new Color(0x0E, 0x0E, 0x12);

    private final Runnable onBack;
    private final Runnable onFullscreen;
    private EmbeddedMediaPlayerComponent media;
    private boolean available;

    private final JLabel titleLabel = new JLabel("", SwingConstants.CENTER);
    private final JComboBox<String> qualityCombo = new JComboBox<String>();
    private final IconButton playPause = new IconButton(IconButton.Kind.PAUSE, 24);
    private final IconButton muteBtn = new IconButton(IconButton.Kind.VOLUME, 22);
    private final JSlider seek = new JSlider(0, 1000, 0);
    private final JSlider volume = new JSlider(0, 100, 90);
    private final JLabel timeLabel = new JLabel("0:00 / 0:00");
    private final Timer timer;

    private JPanel topBar;
    private JPanel bottomBar;
    private Timer hideTimer;
    private boolean immersive = false;
    private Cursor blankCursor;

    private List<String> urls;
    private String fullTitle = "";
    private String currentUrl;
    private boolean seeking = false;
    private boolean updatingCombo = false;

    PlayerPanel(Runnable onBack, Runnable onFullscreen) {
        this.onBack = onBack;
        this.onFullscreen = onFullscreen;
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        // ---- Top bar ----
        IconButton back = new IconButton(IconButton.Kind.BACK, 22);
        back.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { stop(); if (onBack != null) onBack.run(); }
        });
        IconButton ext = new IconButton(IconButton.Kind.EXTERNAL, 22);
        ext.setToolTipText(I18n.t("player.external"));
        ext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { if (currentUrl != null) Player.play(currentUrl); }
        });
        qualityCombo.setFont(Ui.font(12, Font.PLAIN));
        qualityCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (updatingCombo) return;
                int i = qualityCombo.getSelectedIndex();
                if (urls != null && i >= 0 && i < urls.size()) play(urls.get(i));
            }
        });

        JPanel top = new JPanel(new BorderLayout());
        topBar = top;
        top.setBackground(BAR);
        top.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        top.add(back, BorderLayout.WEST);
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topRight.setOpaque(false);
        topRight.add(qualityCombo);
        topRight.add(ext);
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

        // ---- Seek line (slider + total time) ----
        seek.setOpaque(false);
        seek.setForeground(new Color(0xE0, 0x21, 0x21));
        seek.addChangeListener(e -> {
            if (seek.getValueIsAdjusting() && available && media != null) {
                seeking = true;
                media.mediaPlayer().controls().setPosition(seek.getValue() / 1000f);
            } else {
                seeking = false;
            }
        });
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setFont(Ui.font(12, Font.PLAIN));
        JPanel seekLine = new JPanel(new BorderLayout(12, 0));
        seekLine.setOpaque(false);
        seekLine.add(seek, BorderLayout.CENTER);
        seekLine.add(timeLabel, BorderLayout.EAST);

        // ---- Controls ----
        playPause.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { togglePlay(); }
        });
        IconButton back10 = new IconButton(IconButton.Kind.REPLAY10, 24);
        back10.addActionListener(skip(-10000));
        IconButton fwd10 = new IconButton(IconButton.Kind.FORWARD10, 24);
        fwd10.addActionListener(skip(10000));
        muteBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (available && media != null) {
                    media.mediaPlayer().audio().mute();
                    boolean m = media.mediaPlayer().audio().isMute();
                    muteBtn.setKind(m ? IconButton.Kind.MUTE : IconButton.Kind.VOLUME);
                }
            }
        });
        volume.setOpaque(false);
        volume.setPreferredSize(new Dimension(90, 22));
        volume.addChangeListener(e -> {
            if (available && media != null) media.mediaPlayer().audio().setVolume(volume.getValue());
        });
        IconButton fs = new IconButton(IconButton.Kind.FULLSCREEN, 22);
        fs.setToolTipText(I18n.t("btn.fullscreen"));
        fs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { if (onFullscreen != null) onFullscreen.run(); }
        });

        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        leftGroup.setOpaque(false);
        leftGroup.add(playPause);
        leftGroup.add(back10);
        leftGroup.add(fwd10);
        leftGroup.add(muteBtn);
        leftGroup.add(volume);

        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(Ui.font(13, Font.BOLD));

        JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        rightGroup.setOpaque(false);
        rightGroup.add(fs);

        JPanel controls = new JPanel(new BorderLayout(12, 0));
        controls.setOpaque(false);
        controls.add(leftGroup, BorderLayout.WEST);
        controls.add(titleLabel, BorderLayout.CENTER);
        controls.add(rightGroup, BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout(0, 4));
        bottomBar = bottom;
        bottom.setBackground(BAR);
        bottom.setBorder(BorderFactory.createEmptyBorder(6, 14, 8, 14));
        bottom.add(seekLine, BorderLayout.NORTH);
        bottom.add(controls, BorderLayout.CENTER);
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
                Component vs = media.videoSurfaceComponent();
                if (vs != null) { vs.addMouseMotionListener(wake); vs.addMouseListener(wake); }
            } catch (Exception ignored) {}
        }
    }

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
                Component vs = media.videoSurfaceComponent();
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

    void open(String title, List<String> labels, List<String> urls, int startIndex) {
        this.urls = urls;
        this.fullTitle = title == null ? "" : title;
        titleLabel.setText(truncate(fullTitle, 64));
        titleLabel.setToolTipText(fullTitle);
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
        playPause.setKind(IconButton.Kind.PAUSE);
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
        playPause.setKind(playing ? IconButton.Kind.PAUSE : IconButton.Kind.PLAY);
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

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    void stop() {
        timer.stop();
        if (available && media != null) {
            try { media.mediaPlayer().controls().stop(); } catch (Exception ignored) {}
        }
    }
}
