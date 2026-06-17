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
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Adds a right-click Cut / Copy / Paste / Select-all menu to a text field. */
final class ContextMenu {
    private ContextMenu() {}

    static void attach(final JTextComponent field) {
        final JPopupMenu menu = new JPopupMenu();
        final JMenuItem cut = item(I18n.t("ctx.cut"), new ActionListener() {
            public void actionPerformed(ActionEvent e) { field.cut(); }
        });
        final JMenuItem copy = item(I18n.t("ctx.copy"), new ActionListener() {
            public void actionPerformed(ActionEvent e) { field.copy(); }
        });
        final JMenuItem paste = item(I18n.t("ctx.paste"), new ActionListener() {
            public void actionPerformed(ActionEvent e) { field.paste(); }
        });
        final JMenuItem selectAll = item(I18n.t("ctx.selectAll"), new ActionListener() {
            public void actionPerformed(ActionEvent e) { field.selectAll(); }
        });
        menu.add(cut);
        menu.add(copy);
        menu.add(paste);
        menu.addSeparator();
        menu.add(selectAll);

        field.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { maybeShow(e); }
            public void mouseReleased(MouseEvent e) { maybeShow(e); }
            private void maybeShow(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                boolean hasSelection = field.getSelectedText() != null;
                boolean editable = field.isEnabled() && field.isEditable();
                cut.setEnabled(hasSelection && editable);
                copy.setEnabled(hasSelection);
                paste.setEnabled(editable);
                field.requestFocusInWindow();
                menu.show(field, e.getX(), e.getY());
            }
        });
    }

    private static JMenuItem item(String text, ActionListener l) {
        JMenuItem mi = new JMenuItem(text);
        mi.setFont(Ui.font(13, java.awt.Font.PLAIN));
        mi.addActionListener(l);
        return mi;
    }
}
