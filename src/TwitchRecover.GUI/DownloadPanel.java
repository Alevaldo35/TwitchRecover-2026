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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * "Download" screen: paste many links (one per line), pick a folder, and add
 * everything to the download queue at once.
 */
class DownloadPanel extends JPanel {
    private static final String[] QUALITY_PREFS = { "source", "1080", "720", "480", "360", "160", "audio" };
    private static final String[] FORMAT_EXTS = { "mov", "mp4", "mkv", "ts", "avi" };

    private final JTextArea urls;
    private final JLabel folderLabel;
    private final JComboBox<String> qualityCombo;
    private final JComboBox<String> formatCombo;
    private final JLabel status = new JLabel(" ");
    private String destDir;
    private final Runnable openQueue;

    DownloadPanel(Runnable openQueue) {
        this.openQueue = openQueue;
        setBackground(Ui.CANVAS);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(34, 40, 28, 40));

        JLabel title = Ui.title(I18n.t("nav.download"));
        JLabel subtitle = Ui.subtitle(I18n.t("dl.subtitle"));

        urls = new JTextArea();
        urls.setFont(Ui.font(13, Font.PLAIN));
        urls.setLineWrap(false);
        urls.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, I18n.t("dl.placeholder"));
        urls.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        urls.setToolTipText(I18n.t("tip.dlUrls"));
        ContextMenu.attach(urls);
        JScrollPane urlScroll = new JScrollPane(urls);
        urlScroll.setBorder(BorderFactory.createLineBorder(Ui.BORDER, 1, true));
        urlScroll.setPreferredSize(new Dimension(10, 180));

        // Default to C:\Users\<user>\Videos\TWITCH so the user does not have to choose.
        destDir = Paths.twitchDirWithSep();
        JButton chooseFolder = Ui.subtleButton(I18n.t("dl.chooseFolder"));
        folderLabel = new JLabel(Paths.twitchDir().getAbsolutePath());
        folderLabel.setFont(Ui.font(12, Font.PLAIN));
        folderLabel.setForeground(Ui.TEXT);
        chooseFolder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { chooseFolder(); }
        });
        JPanel folderRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        folderRow.setOpaque(false);
        folderRow.add(chooseFolder);
        folderRow.add(folderLabel);

        JLabel qualityLabel = new JLabel(I18n.t("dl.qualityLabel"));
        qualityLabel.setFont(Ui.font(13, Font.PLAIN));
        qualityLabel.setForeground(Ui.TEXT);
        qualityCombo = new JComboBox<String>(new String[]{
                I18n.t("q.best"), "1080p", "720p", "480p", "360p", "160p", I18n.t("q.audioOnly")
        });
        qualityCombo.setFont(Ui.font(13, Font.PLAIN));
        qualityCombo.setMaximumRowCount(8);
        qualityCombo.setPreferredSize(new Dimension(170, 34));
        qualityCombo.setToolTipText(I18n.t("tip.quality"));

        JLabel formatLabel = new JLabel(I18n.t("dl.formatLabel"));
        formatLabel.setFont(Ui.font(13, Font.PLAIN));
        formatLabel.setForeground(Ui.TEXT);
        formatCombo = new JComboBox<String>(new String[]{
                I18n.t("fmt.movRec"), "MP4", "MKV", "TS", "AVI"
        });
        formatCombo.setFont(Ui.font(13, Font.PLAIN));
        formatCombo.setSelectedIndex(0); // MOV recommended, default.
        formatCombo.setPreferredSize(new Dimension(170, 34));
        formatCombo.setToolTipText(I18n.t("tip.format"));

        JPanel qualityRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        qualityRow.setOpaque(false);
        qualityRow.add(qualityLabel);
        qualityRow.add(qualityCombo);
        qualityRow.add(Box.createHorizontalStrut(8));
        qualityRow.add(formatLabel);
        qualityRow.add(formatCombo);

        JButton add = Ui.primaryButton(I18n.t("dl.addQueue"));
        add.setToolTipText(I18n.t("tip.download"));
        add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { addToQueue(); }
        });
        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        addRow.setOpaque(false);
        addRow.add(add);

        status.setFont(Ui.font(12, Font.PLAIN));
        status.setForeground(Ui.TEXT_SECONDARY);

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[]{ title, subtitle, urlScroll, folderRow, qualityRow, addRow, status }) {
            c.setAlignmentX(LEFT_ALIGNMENT);
        }
        col.add(title);
        col.add(Box.createVerticalStrut(6));
        col.add(subtitle);
        col.add(Box.createVerticalStrut(18));
        col.add(urlScroll);
        col.add(Box.createVerticalStrut(14));
        col.add(folderRow);
        col.add(Box.createVerticalStrut(12));
        col.add(qualityRow);
        col.add(Box.createVerticalStrut(18));
        col.add(addRow);
        col.add(Box.createVerticalStrut(12));
        col.add(status);

        add(col, BorderLayout.NORTH);
    }

    /** Pre-fill the link field and focus, used when arriving from another page. */
    void prefill(String url) {
        if (url == null) return;
        String existing = urls.getText().trim();
        urls.setText(existing.isEmpty() ? url : existing + "\n" + url);
        urls.requestFocusInWindow();
        status.setText(" ");
    }

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(I18n.t("fc.title"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            destDir = chooser.getSelectedFile().getAbsolutePath() + File.separator;
            folderLabel.setText(chooser.getSelectedFile().getAbsolutePath());
            folderLabel.setForeground(Ui.TEXT);
        }
    }

    private void addToQueue() {
        if (destDir == null) { status.setText(I18n.t("dl.needFolder")); return; }
        String quality = QUALITY_PREFS[qualityCombo.getSelectedIndex()];
        String format = FORMAT_EXTS[formatCombo.getSelectedIndex()];
        String[] lines = urls.getText().split("\\r?\\n");
        int count = 0;
        int skipped = 0;
        java.util.HashSet<String> seen = new java.util.HashSet<String>();
        for (String line : lines) {
            String url = line.trim();
            if (url.isEmpty() || !url.toLowerCase().contains("twitch")) continue;
            // Skip duplicates within this paste and links already downloading.
            if (!seen.add(url) || DownloadManager.get().isActive(url)) { skipped++; continue; }
            String title = (Resolver.isClip(url) ? "Clip" : "Video") + " · " + shortId(url);
            DownloadManager.get().add(new DownloadTask(url, destDir, quality, format, title));
            count++;
        }
        if (count == 0) {
            status.setText(skipped > 0 ? I18n.t("dl.allDup") : I18n.t("dl.needLinks"));
            return;
        }
        status.setText(count + I18n.t("dl.addedSuffix") + (skipped > 0 ? I18n.t("dl.skippedSuffix").replace("{n}", String.valueOf(skipped)) : ""));
        urls.setText("");
        if (openQueue != null) openQueue.run();
    }

    private String shortId(String url) {
        String s = url;
        int q = s.indexOf('?');
        if (q > 0) s = s.substring(0, q);
        if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        int slash = s.lastIndexOf('/');
        return slash >= 0 ? s.substring(slash + 1) : s;
    }
}
