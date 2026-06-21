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

import TwitchRecover.Core.Clips;
import TwitchRecover.Core.Feeds;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** "Gallery": a Twitch-like grid of thumbnails for every video, marking downloaded vs streamed. */
class GalleryPanel extends JPanel {
    private static final int CARD_W = 324;
    private static final int THUMB_H = 182;

    private final JPanel grid = new JPanel(new WrapLayout(FlowLayout.LEFT, 18, 18));
    private final JLabel status = new JLabel(" ");
    private Nav nav;

    GalleryPanel() {
        setBackground(Ui.CANVAS);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(34, 40, 28, 40));

        JLabel title = Ui.title(I18n.t("nav.gallery"));
        JLabel subtitle = Ui.subtitle(I18n.t("gal.subtitle"));
        JButton clear = Ui.subtleButton(I18n.t("gal.clear"));
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int r = JOptionPane.showConfirmDialog(GalleryPanel.this, I18n.t("gal.clearConfirm"),
                        I18n.t("gal.clear"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (r == JOptionPane.OK_OPTION) { Library.get().clear(); refresh(); }
            }
        });

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        title.setAlignmentX(LEFT_ALIGNMENT);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        left.add(title);
        left.add(Box.createVerticalStrut(6));
        left.add(subtitle);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(left, BorderLayout.WEST);
        JPanel cw = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        cw.setOpaque(false);
        cw.add(clear);
        head.add(cw, BorderLayout.EAST);
        head.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        add(head, BorderLayout.NORTH);

        grid.setOpaque(false);
        JScrollPane scroll = new JScrollPane(grid,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        status.setFont(Ui.font(12, Font.PLAIN));
        status.setForeground(Ui.TEXT_SECONDARY);
        status.setBorder(BorderFactory.createEmptyBorder(10, 2, 0, 0));
        add(status, BorderLayout.SOUTH);
    }

    void setNav(Nav nav) { this.nav = nav; }

    void refresh() {
        List<LibraryEntry> entries = Library.get().snapshot();
        grid.removeAll();
        if (entries.isEmpty()) {
            JLabel l = new JLabel(I18n.t("gal.empty"));
            l.setFont(Ui.font(14, Font.PLAIN));
            l.setForeground(Ui.TEXT_SECONDARY);
            grid.add(l);
        } else {
            for (LibraryEntry e : entries) grid.add(card(e));
        }
        grid.revalidate();
        grid.repaint();
    }

    private JComponent card(final LibraryEntry e) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Ui.CARD);
        Border line = BorderFactory.createLineBorder(Ui.BORDER, 1, true);
        card.setBorder(BorderFactory.createCompoundBorder(line, BorderFactory.createEmptyBorder(0, 0, 12, 0)));
        card.setPreferredSize(new Dimension(CARD_W, THUMB_H + 132));
        card.setMaximumSize(new Dimension(CARD_W, THUMB_H + 132));

        // Thumbnail (clickable to play).
        final JLabel thumb = new JLabel();
        thumb.setHorizontalAlignment(SwingConstants.CENTER);
        thumb.setVerticalAlignment(SwingConstants.CENTER);
        thumb.setPreferredSize(new Dimension(CARD_W, THUMB_H));
        thumb.setMaximumSize(new Dimension(CARD_W, THUMB_H));
        thumb.setOpaque(true);
        thumb.setBackground(new Color(0x1A, 0x1A, 0x1E));
        thumb.setForeground(new Color(0xBB, 0xBB, 0xC2));
        thumb.setFont(Ui.font(12, Font.PLAIN));
        thumb.setText(I18n.t("btn.play"));
        thumb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        thumb.setAlignmentX(LEFT_ALIGNMENT);
        thumb.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent ev) { play(e); }
        });
        loadThumb(thumb, e.thumbnailUrl);
        card.add(thumb);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
        info.setAlignmentX(LEFT_ALIGNMENT);

        JLabel name = new JLabel("<html><div style='width:" + (CARD_W - 28) + "px'>" + escape(e.title == null ? e.url : e.title) + "</div></html>");
        name.setFont(Ui.font(13, Font.BOLD));
        name.setForeground(Ui.TEXT);
        name.setAlignmentX(LEFT_ALIGNMENT);

        JComponent badge = badge(e.downloaded);
        badge.setAlignmentX(LEFT_ALIGNMENT);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(LEFT_ALIGNMENT);
        JButton play = Ui.primaryButton(I18n.t("btn.play"));
        play.setFont(Ui.font(12, Font.BOLD));
        play.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        play.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) { play(e); }
        });
        actions.add(play);
        if (e.downloaded && e.filePath != null) {
            JButton folder = Ui.subtleButton(I18n.t("btn.folderShort"));
            folder.setFont(Ui.font(12, Font.PLAIN));
            folder.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            folder.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) { Player.openFolder(e.filePath); }
            });
            actions.add(folder);
        } else {
            JButton dl = Ui.subtleButton(I18n.t("btn.download"));
            dl.setFont(Ui.font(12, Font.PLAIN));
            dl.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            dl.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) { if (nav != null) nav.openDownload(e.url); }
            });
            actions.add(dl);
        }
        JButton del = Ui.subtleButton(I18n.t("btn.delete"));
        del.setFont(Ui.font(12, Font.PLAIN));
        del.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        del.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) { delete(e); }
        });
        actions.add(del);

        info.add(name);
        info.add(Box.createVerticalStrut(8));
        info.add(badge);
        info.add(Box.createVerticalStrut(10));
        info.add(actions);
        card.add(info);
        return card;
    }

    private void play(final LibraryEntry e) {
        if (nav == null) return;
        if (e.downloaded && e.filePath != null) {
            List<String> labels = new ArrayList<String>();
            List<String> urls = new ArrayList<String>();
            labels.add(I18n.t("badge.downloaded"));
            urls.add(e.filePath);
            nav.openPlayer(e.title, labels, urls, 0);
            return;
        }
        status.setText(I18n.t("player.loading"));
        final boolean clip = e.clip;
        new SwingWorker<Object, Void>() {
            protected Object doInBackground() {
                if (clip) {
                    Clips c = new Clips();
                    return c.retrieveURL(e.url, false);
                }
                return Resolver.videoFeeds(e.url);
            }
            protected void done() {
                status.setText(" ");
                List<String> labels = new ArrayList<String>();
                List<String> urls = new ArrayList<String>();
                try {
                    Object r = get();
                    if (clip) {
                        String link = (String) r;
                        if (link == null || link.isEmpty()) link = e.playLink;
                        labels.add("Clip"); urls.add(link);
                    } else {
                        Feeds f = (Feeds) r;
                        if (f != null && !f.getFeeds().isEmpty()) {
                            for (int i = 0; i < f.getFeeds().size(); i++) {
                                labels.add(f.getQuality(i) == null ? I18n.t("res.unknown") : f.getQuality(i).text);
                                urls.add(f.getFeeds().get(i));
                            }
                        } else if (e.playLink != null) {
                            labels.add(I18n.t("res.unknown")); urls.add(e.playLink);
                        }
                    }
                } catch (Exception ex) {
                    if (e.playLink != null) { labels.add(I18n.t("res.unknown")); urls.add(e.playLink); }
                }
                if (!urls.isEmpty()) nav.openPlayer(e.title, labels, urls, 0);
                else status.setText(I18n.t("st.noLinks"));
            }
        }.execute();
    }

    private void delete(final LibraryEntry e) {
        final boolean hasFile = e.downloaded && e.filePath != null && new File(e.filePath).exists();
        String msg = I18n.t(hasFile ? "gal.deleteFile" : "gal.deleteEntry");
        int r = JOptionPane.showConfirmDialog(this, msg, I18n.t("btn.delete"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;

        // No file on disk: just remove the gallery entry.
        if (!hasFile) {
            Library.get().remove(e.url);
            refresh();
            return;
        }

        // Release the internal player first (on Windows it can keep the file locked),
        // then delete on a background thread with retries so a brief lock doesn't leave
        // the file behind. Only drop the gallery entry once the file is really gone.
        if (nav != null) nav.stopPlayer();
        status.setText(I18n.t("gal.deleting"));
        final File file = new File(e.filePath);
        new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() { return deleteFile(file); }
            protected void done() {
                boolean ok;
                try { ok = get(); } catch (Exception ex) { ok = !file.exists(); }
                status.setText(" ");
                if (ok) {
                    Library.get().remove(e.url);
                    refresh();
                } else {
                    // Keep the entry so the user can close whatever holds the file and retry.
                    JOptionPane.showMessageDialog(GalleryPanel.this, I18n.t("gal.deleteFail"),
                            I18n.t("btn.delete"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /** Delete a file, retrying briefly: a player (vlcj/JNA) may release the handle a moment late. */
    private static boolean deleteFile(File f) {
        if (f == null) return true;
        for (int i = 0; i < 6; i++) {
            if (!f.exists()) return true;
            if (f.delete()) return true;
            System.gc();   // nudge the JVM to release any lingering native file handle
            try { Thread.sleep(150); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
        return !f.exists();
    }

    private void loadThumb(final JLabel label, final String url) {
        if (url == null || url.isEmpty()) return;
        new SwingWorker<ImageIcon, Void>() {
            protected ImageIcon doInBackground() {
                java.io.InputStream in = null;
                try {
                    // Use explicit timeouts so a slow thumbnail host can't hang this worker thread.
                    java.net.HttpURLConnection con = (java.net.HttpURLConnection) new URL(url).openConnection();
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    con.setRequestProperty("User-Agent", "Mozilla/5.0");
                    in = con.getInputStream();
                    BufferedImage img = ImageIO.read(in);
                    if (img == null) return null;
                    Image scaled = img.getScaledInstance(CARD_W, THUMB_H, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                } catch (Exception ex) {
                    return null;
                } finally {
                    if (in != null) try { in.close(); } catch (Exception ignored) {}
                }
            }
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) { label.setText(""); label.setIcon(icon); }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private JComponent badge(boolean downloaded) {
        JLabel b = new JLabel(I18n.t(downloaded ? "badge.downloaded" : "badge.streamed"));
        b.setFont(Ui.font(11, Font.BOLD));
        Color bg = downloaded ? new Color(0xE2, 0xF3, 0xE6) : new Color(0xEC, 0xE5, 0xFF);
        Color fg = downloaded ? new Color(0x2E, 0xA0, 0x43) : Ui.ACCENT;
        b.setOpaque(true);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrap.setOpaque(false);
        wrap.add(b);
        return wrap;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
