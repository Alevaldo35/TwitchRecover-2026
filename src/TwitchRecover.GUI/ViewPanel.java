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

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** "View" screen: paste any Twitch link, auto-detect its type, retrieve and copy the links. */
class ViewPanel extends JPanel {
    private final JTextField urlField;
    private final JButton goButton;
    private final JPanel results = new JPanel();
    private final JLabel status = new JLabel(" ");
    private final Nav nav;
    private String currentUrl = "";
    private String currentTitle = "";
    private final java.util.List<String> curLabels = new java.util.ArrayList<String>();
    private final java.util.List<String> curUrls = new java.util.ArrayList<String>();

    ViewPanel(Nav nav) {
        this.nav = nav;
        setBackground(Ui.CANVAS);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(34, 40, 28, 40));

        JLabel title = Ui.title(I18n.t("nav.view"));
        JLabel subtitle = Ui.subtitle(I18n.t("sub.viewAny"));

        urlField = Ui.field(I18n.t("ph.any"));
        urlField.setToolTipText(I18n.t("tip.urlField"));
        ContextMenu.attach(urlField);
        goButton = Ui.primaryButton(I18n.t("act.getLinks"));
        goButton.setToolTipText(I18n.t("tip.getLinks"));
        ActionListener fetch = new ActionListener() {
            public void actionPerformed(ActionEvent e) { fetch(); }
        };
        goButton.addActionListener(fetch);
        urlField.addActionListener(fetch);

        JPanel inputRow = new JPanel(new BorderLayout(12, 0));
        inputRow.setOpaque(false);
        inputRow.add(urlField, BorderLayout.CENTER);
        inputRow.add(goButton, BorderLayout.EAST);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        title.setAlignmentX(LEFT_ALIGNMENT);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        inputRow.setAlignmentX(LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(18));
        header.add(inputRow);
        add(header, BorderLayout.NORTH);

        results.setOpaque(false);
        results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(results,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        status.setFont(Ui.font(12, Font.PLAIN));
        status.setForeground(Ui.TEXT_SECONDARY);
        status.setBorder(BorderFactory.createEmptyBorder(10, 2, 0, 0));
        add(status, BorderLayout.SOUTH);

        showEmpty(I18n.t("empty.anyHint"));
    }

    private void fetch() {
        final String url = urlField.getText().trim();
        if (url.isEmpty()) { status.setText(I18n.t("st.pasteFirst")); return; }
        currentUrl = url;
        setBusy(true);
        status.setText(I18n.t("st.fetching"));
        results.removeAll();
        results.revalidate();
        results.repaint();

        new SwingWorker<Object, Void>() {
            boolean clip;
            Meta meta;
            protected Object doInBackground() {
                clip = Resolver.isClip(url);
                meta = Meta.fetch(url);
                if (clip) {
                    Clips c = new Clips();
                    return c.retrieveURL(url, false);
                }
                return Resolver.videoFeeds(url);
            }
            protected void done() {
                setBusy(false);
                try {
                    Object r = get();
                    String nice = meta.display(shortTitle(url, clip));
                    currentTitle = nice;
                    curLabels.clear();
                    curUrls.clear();
                    if (clip) {
                        String link = (String) r;
                        if (link == null || link.isEmpty()) {
                            status.setText(I18n.t("st.noClip"));
                            showEmpty(I18n.t("empty.noClip"));
                        } else {
                            status.setText(nice);
                            curLabels.add("Clip");
                            curUrls.add(link);
                            Library.get().addStreamed(url, nice, true, link, meta.thumbnailUrl);
                            results.removeAll();
                            results.add(row(I18n.t("res.permalink"), link, 0));
                            results.revalidate(); results.repaint();
                        }
                    } else {
                        Feeds f = (Feeds) r;
                        if (f == null || f.getFeeds().isEmpty()) {
                            status.setText(I18n.t("st.noLinks"));
                            showEmpty(I18n.t("empty.none"));
                        } else {
                            status.setText(nice + "   ·   " + f.getFeeds().size() + I18n.t("st.foundSuffix"));
                            Library.get().addStreamed(url, nice, false, Resolver.pickFeed(f, "source"), meta.thumbnailUrl);
                            results.removeAll();
                            for (int i = 0; i < f.getFeeds().size(); i++) {
                                String q = f.getQuality(i) == null ? I18n.t("res.unknown") : f.getQuality(i).text;
                                curLabels.add(q);
                                curUrls.add(f.getFeeds().get(i));
                                results.add(row(q, f.getFeeds().get(i), i));
                                results.add(Box.createVerticalStrut(10));
                            }
                            results.revalidate(); results.repaint();
                        }
                    }
                } catch (Exception ex) {
                    status.setText(I18n.t("st.error") + ex.getMessage());
                    showEmpty(I18n.t("empty.none"));
                }
            }
        }.execute();
    }

    private void setBusy(boolean busy) {
        goButton.setEnabled(!busy);
        urlField.setEnabled(!busy);
        goButton.setText(busy ? I18n.t("act.working") : I18n.t("act.getLinks"));
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private void showEmpty(String msg) {
        results.removeAll();
        JLabel l = new JLabel(msg);
        l.setFont(Ui.font(14, Font.PLAIN));
        l.setForeground(Ui.TEXT_SECONDARY);
        l.setAlignmentX(LEFT_ALIGNMENT);
        results.add(l);
        results.revalidate();
        results.repaint();
    }

    private JComponent row(String quality, final String link, final int index) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(Ui.CARD);
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        Border line = BorderFactory.createLineBorder(Ui.BORDER, 1, true);
        card.setBorder(BorderFactory.createCompoundBorder(line, BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        JLabel q = new JLabel(quality);
        q.setFont(Ui.font(14, Font.BOLD));
        q.setForeground(Ui.TEXT);
        q.setAlignmentX(LEFT_ALIGNMENT);
        JLabel u = new JLabel(truncate(link, 78));
        u.setFont(Ui.font(11, Font.PLAIN));
        u.setForeground(Ui.TEXT_SECONDARY);
        u.setAlignmentX(LEFT_ALIGNMENT);
        texts.add(q);
        texts.add(Box.createVerticalStrut(3));
        texts.add(u);

        JButton play = Ui.primaryButton(I18n.t("btn.play"));
        play.setFont(Ui.font(13, Font.BOLD));
        play.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        play.setToolTipText(I18n.t("tip.play"));
        play.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (nav != null) nav.openPlayer(currentTitle, curLabels, curUrls, index);
            }
        });

        JButton download = Ui.subtleButton(I18n.t("btn.download"));
        download.setToolTipText(I18n.t("tip.download"));
        download.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (nav != null) nav.openDownload(currentUrl);
            }
        });

        JButton copy = Ui.subtleButton(I18n.t("btn.copy"));
        copy.setToolTipText(I18n.t("tip.copy"));
        copy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(link), null);
                status.setText(I18n.t("st.copied"));
            }
        });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(play);
        right.add(download);
        right.add(copy);

        card.add(texts, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private String shortTitle(String url, boolean clip) {
        String s = url;
        int q = s.indexOf('?');
        if (q > 0) s = s.substring(0, q);
        if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        int slash = s.lastIndexOf('/');
        String id = slash >= 0 ? s.substring(slash + 1) : s;
        return (clip ? "Clip" : "VOD") + " " + id;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
