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
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/** "Downloads" queue page: shows every task with a live progress bar. */
class DownloadsPanel extends JPanel {
    private final JPanel list = new JPanel();
    private final Timer refresh;

    DownloadsPanel() {
        setBackground(Ui.CANVAS);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(34, 40, 28, 40));

        JLabel title = Ui.title(I18n.t("nav.queue"));
        JLabel subtitle = Ui.subtitle(I18n.t("q.subtitle"));
        JButton clear = Ui.subtleButton(I18n.t("q.clear"));
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { DownloadManager.get().clearFinished(); rebuild(); }
        });

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        title.setAlignmentX(LEFT_ALIGNMENT);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        left.add(title);
        left.add(Box.createVerticalStrut(6));
        left.add(subtitle);
        head.add(left, BorderLayout.WEST);
        JPanel clearWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        clearWrap.setOpaque(false);
        clearWrap.add(clear);
        head.add(clearWrap, BorderLayout.EAST);
        head.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        add(head, BorderLayout.NORTH);

        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(list,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        refresh = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) { rebuild(); }
        });
        rebuild();
    }

    void start() { refresh.start(); }
    void stop() { refresh.stop(); }

    private void rebuild() {
        List<DownloadTask> tasks = DownloadManager.get().snapshot();
        list.removeAll();
        if (tasks.isEmpty()) {
            JLabel l = new JLabel(I18n.t("q.empty"));
            l.setFont(Ui.font(14, Font.PLAIN));
            l.setForeground(Ui.TEXT_SECONDARY);
            l.setAlignmentX(LEFT_ALIGNMENT);
            list.add(l);
        } else {
            for (DownloadTask t : tasks) {
                list.add(card(t));
                list.add(Box.createVerticalStrut(10));
            }
        }
        list.revalidate();
        list.repaint();
    }

    private JComponent card(DownloadTask t) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Ui.CARD);
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        Border line = BorderFactory.createLineBorder(Ui.BORDER, 1, true);
        card.setBorder(BorderFactory.createCompoundBorder(line, BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel name = new JLabel(t.title);
        name.setFont(Ui.font(14, Font.BOLD));
        name.setForeground(Ui.TEXT);
        JLabel state = new JLabel(stateText(t));
        state.setFont(Ui.font(12, Font.PLAIN));
        state.setForeground(stateColor(t));
        top.add(name, BorderLayout.WEST);
        top.add(state, BorderLayout.EAST);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setBorderPainted(false);
        int pct = t.percent();
        if (t.state == DownloadTask.State.DOWNLOADING && pct < 0) {
            bar.setIndeterminate(true);
        } else {
            bar.setIndeterminate(false);
            bar.setValue(Math.max(0, pct));
        }
        bar.setForeground(t.state == DownloadTask.State.FAILED ? new Color(0xE0, 0x4F, 0x4F) : Ui.ACCENT);
        bar.setPreferredSize(new Dimension(10, 8));

        JLabel sub = new JLabel(subText(t));
        sub.setFont(Ui.font(11, Font.PLAIN));
        sub.setForeground(Ui.TEXT_SECONDARY);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(bar, BorderLayout.CENTER);

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        top.setAlignmentX(LEFT_ALIGNMENT);
        bottom.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        inner.add(top);
        inner.add(Box.createVerticalStrut(8));
        inner.add(bottom);
        inner.add(Box.createVerticalStrut(6));
        inner.add(sub);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private String stateText(DownloadTask t) {
        switch (t.state) {
            case QUEUED: return I18n.t("st.queued");
            case DOWNLOADING: return I18n.t("st.downloading2");
            case DONE: return I18n.t("st.finished");
            default: return I18n.t("st.failed");
        }
    }

    private Color stateColor(DownloadTask t) {
        switch (t.state) {
            case DONE: return new Color(0x2E, 0xA0, 0x43);
            case FAILED: return new Color(0xE0, 0x4F, 0x4F);
            case DOWNLOADING: return Ui.ACCENT;
            default: return Ui.TEXT_SECONDARY;
        }
    }

    private String subText(DownloadTask t) {
        if (t.state == DownloadTask.State.DONE && t.outputPath != null) return I18n.t("st.saved") + t.outputPath;
        if (t.state == DownloadTask.State.FAILED) return I18n.t("st.dlFail") + (t.error == null ? "" : t.error);
        if (t.state == DownloadTask.State.DOWNLOADING) {
            if (t.total > 0) return t.done + " / " + t.total;
            return I18n.t("q.preparing");
        }
        return t.sourceUrl;
    }
}
