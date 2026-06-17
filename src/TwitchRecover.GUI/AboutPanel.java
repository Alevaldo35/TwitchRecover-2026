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
import java.net.URI;

/** Credits / about screen. Keeps full credit with the original author. */
class AboutPanel extends JPanel {
    AboutPanel() {
        setBackground(Ui.CANVAS);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(34, 40, 28, 40));

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        col.add(left(Ui.title(I18n.t("about.title"))));
        col.add(Box.createVerticalStrut(6));
        col.add(left(Ui.subtitle(I18n.t("about.subtitle"))));
        col.add(Box.createVerticalStrut(24));

        // This fork (Alevaldo35) first.
        col.add(left(Ui.sectionLabel(I18n.t("about.fork"))));
        col.add(Box.createVerticalStrut(6));
        col.add(left(body(I18n.t("about.forkBody"))));
        col.add(Box.createVerticalStrut(8));
        col.add(left(linkButton("github.com/Alevaldo35",
                "https://github.com/Alevaldo35")));

        // Support / donation.
        col.add(Box.createVerticalStrut(24));
        col.add(left(Ui.sectionLabel(I18n.t("about.support"))));
        col.add(Box.createVerticalStrut(6));
        col.add(left(body(I18n.t("about.supportBody"))));
        col.add(Box.createVerticalStrut(8));
        col.add(left(linkButton(I18n.t("about.donate"), "https://paypal.me/AlexValdoBH77")));

        // Original project credit.
        col.add(Box.createVerticalStrut(24));
        col.add(left(Ui.sectionLabel(I18n.t("about.original"))));
        col.add(Box.createVerticalStrut(6));
        col.add(left(body(I18n.t("about.originalBody"))));
        col.add(Box.createVerticalStrut(8));
        col.add(left(linkButton("github.com/TwitchRecover/TwitchRecover",
                "https://github.com/TwitchRecover/TwitchRecover")));

        col.add(Box.createVerticalStrut(24));
        col.add(left(Ui.sectionLabel(I18n.t("about.license"))));
        col.add(Box.createVerticalStrut(6));
        col.add(left(body(I18n.t("about.licenseBody"))));

        add(col, BorderLayout.NORTH);
    }

    private JComponent left(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(c);
        return wrap;
    }

    private JLabel body(String text) {
        JLabel l = new JLabel("<html><div style='width:560px'>" + text + "</div></html>");
        l.setFont(Ui.font(14, Font.PLAIN));
        l.setForeground(Ui.TEXT);
        return l;
    }

    private JButton linkButton(String text, final String url) {
        JButton b = new JButton(text);
        b.setFont(Ui.font(14, Font.BOLD));
        b.setForeground(Ui.ACCENT);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI(url));
                    }
                } catch (Exception ignored) {}
            }
        });
        return b;
    }
}
