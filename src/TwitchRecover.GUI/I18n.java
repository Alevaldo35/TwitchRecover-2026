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

import java.util.HashMap;
import java.util.Map;

/**
 * Minimal two-language (French / English) string table.
 * French is the default language.
 */
final class I18n {
    enum Lang { FR, EN }

    static Lang current = Lang.FR;

    private static final Map<String, String[]> T = new HashMap<String, String[]>();
    // Each entry: [0] = French, [1] = English.
    private static void p(String key, String fr, String en) { T.put(key, new String[]{ fr, en }); }

    static {
        p("brand.tagline", "Récupérer • Télécharger", "Recover • Download");

        p("nav.view", "Voir", "View");
        p("nav.download", "Télécharger", "Download");
        p("nav.queue", "Téléchargements", "Downloads");
        p("nav.gallery", "Galerie", "Gallery");
        p("nav.about", "À propos", "About");

        // Gallery
        p("gal.subtitle", "Toutes vos vidéos : celles téléchargées et celles regardées en stream.",
                          "All your videos: the ones you downloaded and the ones you watched as a stream.");
        p("gal.empty", "Galerie vide. Récupérez ou téléchargez une vidéo pour la retrouver ici.",
                       "Empty gallery. Retrieve or download a video to find it here.");
        p("gal.clear", "Vider la galerie", "Clear gallery");
        p("gal.clearConfirm", "Vider la galerie ?\n\nCela retire seulement les vignettes de la liste.\nVos fichiers vidéo déjà téléchargés restent intacts dans le dossier Vidéos\\TWITCH.",
                              "Clear the gallery?\n\nThis only removes the thumbnails from the list.\nYour already-downloaded video files stay safe in the Videos\\TWITCH folder.");
        p("badge.downloaded", "Téléchargé", "Downloaded");
        p("badge.streamed", "Vu en stream", "Watched (stream)");
        p("btn.play", "Lire", "Play");
        p("btn.openFolder", "Ouvrir le dossier", "Open folder");
        p("btn.folderShort", "Dossier", "Folder");
        p("btn.delete", "Supprimer", "Delete");
        p("gal.deleteFile", "Supprimer cette vidéo ?\n\nLe fichier sera définitivement supprimé du disque pour libérer de l'espace.",
                            "Delete this video?\n\nThe file will be permanently removed from disk to free up space.");
        p("gal.deleteEntry", "Retirer cette vidéo de la galerie ?", "Remove this video from the gallery?");
        p("st.playing", "Ouverture dans VLC…", "Opening in VLC…");
        p("st.noPlayer", "VLC introuvable. Installez VLC (videolan.org) ou utilisez « Copier le lien ».",
                         "VLC not found. Install VLC (videolan.org) or use “Copy link”.");
        p("dl.defaultFolder", "Par défaut : dossier Vidéos\\TWITCH", "Default: Videos\\TWITCH folder");

        p("type.vod", "VOD", "VOD");
        p("type.highlight", "Temps fort", "Highlight");
        p("type.clip", "Clip", "Clip");

        p("sub.viewAny", "Collez n'importe quel lien Twitch (VOD, temps fort ou clip) — y compris les VOD sub-only.",
                         "Paste any Twitch link (VOD, highlight or clip) — including sub-only VODs.");
        p("ph.any", "https://www.twitch.tv/videos/123456789   ou   https://clips.twitch.tv/…",
                    "https://www.twitch.tv/videos/123456789   or   https://clips.twitch.tv/…");
        p("empty.anyHint", "Astuce : faites un clic droit dans le champ ▸ Coller, puis cliquez sur « Obtenir les liens ».",
                           "Tip: right-click the field ▸ Paste, then press “Get links”.");

        // Right-click menu
        p("ctx.cut", "Couper", "Cut");
        p("ctx.copy", "Copier", "Copy");
        p("ctx.paste", "Coller", "Paste");
        p("ctx.selectAll", "Tout sélectionner", "Select all");

        // Tooltips / beginner hints
        p("tip.urlField", "Collez ici le lien d'une VOD, d'un temps fort ou d'un clip Twitch (clic droit ▸ Coller).",
                          "Paste the link of a Twitch VOD, highlight or clip here (right-click ▸ Paste).");
        p("tip.getLinks", "Récupère les liens lisibles de la vidéo.", "Retrieves the playable links of the video.");
        p("tip.copy", "Copie le lien pour le coller ailleurs (ex. dans VLC).", "Copies the link to paste elsewhere (e.g. into VLC).");
        p("tip.play", "Lit la vidéo dans le lecteur intégré.", "Plays the video in the built-in player.");
        p("tip.download", "Envoie la vidéo dans la file de téléchargement.", "Sends the video to the download queue.");
        p("tip.dlUrls", "Un lien par ligne. Clic droit ▸ Coller pour ajouter rapidement.",
                        "One link per line. Right-click ▸ Paste to add quickly.");
        p("tip.quality", "Qualité de la vidéo téléchargée.", "Quality of the downloaded video.");
        p("tip.format", "Format du fichier. MOV est recommandé.", "File format. MOV is recommended.");
        p("tip.lang", "Changer la langue / Switch language.", "Changer la langue / Switch language.");

        p("act.getLinks", "Obtenir les liens", "Get links");
        p("act.working", "Patientez…", "Working…");

        p("btn.copy", "Copier le lien", "Copy link");
        p("btn.download", "Télécharger", "Download");

        p("st.pasteFirst", "Collez d'abord un lien Twitch.", "Please paste a Twitch link first.");
        p("st.fetching", "Récupération…", "Fetching…");
        p("st.copied", "Lien copié — collez-le dans VLC (Média ▸ Ouvrir un flux réseau).",
                       "Link copied — paste it into VLC (Media ▸ Open Network Stream).");
        p("st.noClip", "Aucun clip trouvé pour ce lien.", "No clip found for that link.");
        p("st.noLinks", "Aucun lien trouvé.", "No links found.");
        p("st.foundSuffix", " qualité(s) trouvée(s).", " quality option(s) found.");
        p("st.done", "Terminé.", "Done.");
        p("st.error", "Une erreur est survenue : ", "Something went wrong: ");
        p("st.downloading", "Téléchargement… cela peut être long pour les longues vidéos.",
                            "Downloading… this can take a while for long videos.");
        p("st.saved", "Enregistré dans : ", "Saved to: ");
        p("st.dlDone", "Téléchargement terminé.", "Download finished.");
        p("st.dlFail", "Échec du téléchargement : ", "Download failed: ");

        p("empty.viewHint", "Collez un lien ci-dessus, puis cliquez sur « Obtenir les liens ».",
                            "Paste a link above, then press “Get links”.");
        p("empty.clipHint", "Collez un lien de clip ci-dessus, puis cliquez sur « Obtenir le lien ».",
                            "Paste a clip link above, then press “Get link”.");
        p("empty.none", "Rien trouvé. La vidéo est peut-être supprimée, privée, ou le lien est incorrect.",
                        "Nothing found. The video may be deleted, private, or the link is wrong.");
        p("empty.noClip", "Aucun clip trouvé. Vérifiez le lien et réessayez.",
                          "No clip found. Check the link and try again.");

        p("res.permalink", "Lien permanent", "Permanent link");
        p("res.unknown", "Inconnu", "Unknown");

        p("fc.title", "Choisissez un dossier de destination", "Choose a folder to save into");

        p("about.title", "À propos", "About");
        p("about.subtitle", "Un outil gratuit pour voir, récupérer et télécharger des vidéos Twitch.",
                            "A free tool to view, recover and download Twitch videos.");
        p("about.original", "PROJET ORIGINAL", "ORIGINAL PROJECT");
        p("about.originalBody", "Twitch Recover par Daylam Tayari — tout le code original et le crédit reviennent à l'auteur.",
                                "Twitch Recover by Daylam Tayari — all original code and credit belong to the author.");
        p("about.fork", "CE FORK", "THIS FORK");
        p("about.forkBody", "Fork non officiel corrigé — corrections pour relancer l'app et récupérer à nouveau les liens VOD, plus cette interface épurée.",
                            "Unofficial patched fork — fixes so the app runs and retrieves VOD links again, plus this clean GUI.");
        p("about.license", "LICENCE", "LICENSE");
        p("about.licenseBody", "Distribué sous licence GNU GPLv3, la même que le projet original.",
                               "Distributed under the GNU GPLv3, the same license as the original project.");

        // Download (bulk add) panel
        p("dl.subtitle", "Collez un ou plusieurs liens (un par ligne). Tout sera ajouté à la file d'attente.",
                         "Paste one or more links (one per line). Everything is added to the queue.");
        p("dl.placeholder", "https://www.twitch.tv/videos/123456789\nhttps://www.twitch.tv/videos/987654321\n…",
                            "https://www.twitch.tv/videos/123456789\nhttps://www.twitch.tv/videos/987654321\n…");
        p("dl.chooseFolder", "Choisir le dossier", "Choose folder");
        p("dl.noFolder", "Aucun dossier choisi", "No folder chosen");
        p("dl.qualityLabel", "Qualité :", "Quality:");
        p("dl.formatLabel", "Format :", "Format:");
        p("fmt.movRec", "MOV (recommandé)", "MOV (recommended)");
        p("q.best", "Meilleure (source)", "Best (source)");
        p("q.audioOnly", "Audio seul", "Audio only");
        p("dl.addQueue", "Ajouter à la file", "Add to queue");
        p("dl.needFolder", "Choisissez d'abord un dossier de destination.", "Choose a destination folder first.");
        p("dl.needLinks", "Collez au moins un lien.", "Paste at least one link.");
        p("dl.addedSuffix", " lien(s) ajouté(s) à la file d'attente.", " link(s) added to the queue.");

        // Downloads (queue) page
        p("q.subtitle", "Tous vos téléchargements. Ils se lancent automatiquement, un par un.",
                        "All your downloads. They start automatically, one after another.");
        p("q.empty", "Aucun téléchargement pour l'instant. Ajoutez-en depuis l'onglet « Télécharger ».",
                     "No downloads yet. Add some from the “Download” tab.");
        p("q.clear", "Effacer terminés", "Clear finished");
        p("st.queued", "En file", "Queued");
        p("st.downloading2", "Téléchargement…", "Downloading…");
        p("st.finished", "Terminé", "Done");
        p("st.failed", "Échec", "Failed");
        p("q.preparing", "Préparation…", "Preparing…");

        // Internal player
        p("player.back", "‹ Retour", "‹ Back");
        p("btn.pause", "Pause", "Pause");
        p("player.loading", "Chargement de la vidéo…", "Loading video…");
        p("player.unavailable", "Lecteur interne indisponible (libVLC introuvable). La vidéo s'ouvre dans VLC.",
                                "Internal player unavailable (libVLC not found). The video opens in VLC.");
        p("player.openVlc", "Lire dans VLC", "Play in VLC");
        p("btn.stop", "Arrêter", "Stop");
        p("btn.mute", "Muet", "Mute");
        p("btn.unmute", "Son", "Unmute");
        p("btn.back10", "− 10 s", "− 10 s");
        p("btn.fwd10", "+ 10 s", "+ 10 s");
        p("player.volume", "Volume", "Volume");

        // About / support
        p("about.support", "SOUTENIR LE PROJET", "SUPPORT THE PROJECT");
        p("about.supportBody", "Si ce fork vous est utile, vous pouvez soutenir le développement via la cagnotte :",
                               "If this fork is useful to you, you can support development via the tip jar:");
        p("about.donate", "Faire un don — paypal.me/AlexValdoBH77", "Donate — paypal.me/AlexValdoBH77");

        p("lang.current", "Français", "English");
        p("lang.tooltip", "Changer de langue / Switch language", "Changer de langue / Switch language");
    }

    static String t(String key) {
        String[] v = T.get(key);
        if (v == null) return key;
        return v[current == Lang.FR ? 0 : 1];
    }

    static void toggle() {
        current = (current == Lang.FR) ? Lang.EN : Lang.FR;
    }
}
