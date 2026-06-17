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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Main application window: a clean sidebar of action buttons on the left
 * (View, Download, Downloads, About) and a content area on the right.
 */
public class MainWindow extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final ArrayList<SidebarButton> navButtons = new ArrayList<SidebarButton>();
    private DownloadsPanel downloadsPanel;
    private DownloadPanel downloadPanel;
    private GalleryPanel galleryPanel;
    private PlayerPanel playerPanel;
    private String currentCard = "view";

    private final java.util.prefs.Preferences prefs =
            java.util.prefs.Preferences.userRoot().node("twitchrecover/gui");

    public MainWindow() {
        setTitle("Twitch Recover");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 640));

        // Default window size, restoring the last size/position if the user resized it.
        int w = prefs.getInt("w", 1200);
        int h = prefs.getInt("h", 780);
        int x = prefs.getInt("x", Integer.MIN_VALUE);
        int y = prefs.getInt("y", Integer.MIN_VALUE);
        setSize(w, h);
        if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE) setLocationRelativeTo(null);
        else setLocation(x, y);

        // Remember the window geometry for next time.
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) { saveBounds(); }
            public void componentMoved(java.awt.event.ComponentEvent e) { saveBounds(); }
        });

        setAppIcon();
        getContentPane().setBackground(Ui.CANVAS);
        buildUi();
    }

    /** Use the project logo as the window/taskbar icon instead of the default Java cup. */
    private void setAppIcon() {
        try {
            java.net.URL res = getClass().getResource("/logo.png");
            java.awt.Image img = (res != null)
                    ? new ImageIcon(res).getImage()
                    : new ImageIcon("logo.png").getImage();
            if (img != null) {
                java.util.List<java.awt.Image> icons = new ArrayList<java.awt.Image>();
                icons.add(img);
                setIconImages(icons);
            }
        } catch (Exception ignored) {}
    }

    private void saveBounds() {
        if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0) return;
        prefs.putInt("w", getWidth());
        prefs.putInt("h", getHeight());
        prefs.putInt("x", getX());
        prefs.putInt("y", getY());
    }

    private void buildUi() {
        getContentPane().removeAll();
        navButtons.clear();
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        content.removeAll();
        content.setBackground(Ui.CANVAS);
        downloadsPanel = new DownloadsPanel();
        galleryPanel = new GalleryPanel();
        downloadPanel = new DownloadPanel(new Runnable() {
            public void run() { select("downloads"); }
        });
        playerPanel = new PlayerPanel(new Runnable() {
            public void run() { select("view"); }
        });
        Nav nav = new Nav() {
            public void openDownload(String url) {
                downloadPanel.prefill(url);
                select("download");
            }
            public void openPlayer(String title, java.util.List<String> labels, java.util.List<String> urls, int startIndex) {
                select("player");
                playerPanel.open(title, labels, urls, startIndex);
            }
        };
        content.add(new ViewPanel(nav), "view");
        content.add(downloadPanel, "download");
        content.add(downloadsPanel, "downloads");
        galleryPanel.setNav(nav);
        content.add(galleryPanel, "gallery");
        content.add(playerPanel, "player");
        content.add(new AboutPanel(), "about");
        add(content, BorderLayout.CENTER);

        select(currentCard);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private JComponent buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(Ui.SIDEBAR);
        side.setPreferredSize(new Dimension(264, 0));
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Ui.BORDER));

        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setOpaque(false);
        brand.setBorder(BorderFactory.createEmptyBorder(26, 22, 18, 22));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel logo = new JLabel("Twitch Recover");
        logo.setFont(Ui.font(18, Font.BOLD));
        logo.setForeground(Ui.TEXT);
        JLabel tag = new JLabel(I18n.t("brand.tagline"));
        tag.setFont(Ui.font(11, Font.PLAIN));
        tag.setForeground(Ui.TEXT_SECONDARY);
        brand.add(logo);
        brand.add(Box.createVerticalStrut(2));
        brand.add(tag);
        side.add(brand);

        side.add(navItem(I18n.t("nav.view"), "view"));
        side.add(navItem(I18n.t("nav.download"), "download"));
        side.add(navItem(I18n.t("nav.queue"), "downloads"));
        side.add(navItem(I18n.t("nav.gallery"), "gallery"));
        side.add(Box.createVerticalGlue());
        side.add(navItem(I18n.t("nav.about"), "about"));
        side.add(Box.createVerticalStrut(8));
        side.add(languageButton());
        side.add(Box.createVerticalStrut(14));
        return side;
    }

    private JComponent languageButton() {
        final JButton b = new JButton(I18n.t("lang.current"));
        b.setToolTipText(I18n.t("lang.tooltip"));
        b.setFont(Ui.font(13, Font.BOLD));
        b.setForeground(Ui.ACCENT);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
                "arc: 999; borderColor: " + Ui.hex(Ui.BORDER) + "; background: #ffffff");
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        wrap.add(b, BorderLayout.CENTER);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                I18n.toggle();
                buildUi();   // Rebuild the whole UI in the new language.
            }
        });
        return wrap;
    }

    private SidebarButton navItem(String label, final String card) {
        SidebarButton b = new SidebarButton(label);
        b.cardKey = card;
        b.onClick(new Runnable() { public void run() { select(card); } });
        navButtons.add(b);
        return b;
    }

    private void select(String card) {
        currentCard = card;
        cards.show(content, card);
        for (SidebarButton b : navButtons) {
            b.setActive(card.equals(b.cardKey));
        }
        if (downloadsPanel != null) {
            if ("downloads".equals(card)) downloadsPanel.start();
            else downloadsPanel.stop();
        }
        if (galleryPanel != null && "gallery".equals(card)) {
            galleryPanel.refresh();
        }
        if (playerPanel != null && !"player".equals(card)) {
            playerPanel.stop();
        }
    }

    /** A flat, fully rounded sidebar entry (custom-painted, no default button chrome). */
    private static class SidebarButton extends JComponent {
        String cardKey;
        private final String text;
        private boolean active;
        private boolean hover;
        private Runnable onClick;

        SidebarButton(String label) {
            this.text = label;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { hover = true; repaint(); }
                public void mouseExited(java.awt.event.MouseEvent e) { hover = false; repaint(); }
                public void mouseClicked(java.awt.event.MouseEvent e) { if (onClick != null) onClick.run(); }
            });
        }

        void onClick(Runnable r) { this.onClick = r; }

        public Dimension getPreferredSize() { return new Dimension(180, 42); }
        public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 42); }
        public Dimension getMinimumSize() { return new Dimension(80, 42); }

        void setActive(boolean sel) { this.active = sel; repaint(); }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int pad = 10, arc = 12;
            int w = getWidth() - pad * 2, h = getHeight() - 6;
            if (active) {
                g2.setColor(Ui.SELECTED_BG);
                g2.fillRoundRect(pad, 3, w, h, arc, arc);
            } else if (hover) {
                g2.setColor(new Color(0x00, 0x00, 0x00, 12));
                g2.fillRoundRect(pad, 3, w, h, arc, arc);
            }
            g2.setColor(active ? Ui.ACCENT : Ui.TEXT);
            g2.setFont(Ui.font(14, active ? Font.BOLD : Font.PLAIN));
            FontMetrics fm = g2.getFontMetrics();
            int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(text, pad + 16, ty);
            g2.dispose();
        }
    }
}
