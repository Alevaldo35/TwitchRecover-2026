/*
 * Copyright (c) 2020, 2021 Daylam Tayari <daylam@tayari.gg>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License version 3as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not see http://www.gnu.org/licenses/ or write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  @author Daylam Tayari daylam@tayari.gg https://github.com/daylamtayari
 *  @version 2.0aH     2.0a Hotfix
 *  Github project home page: https://github.com/TwitchRecover
 *  Twitch Recover repository: https://github.com/TwitchRecover/TwitchRecover
 */

package TwitchRecover.Core.Downloader;

import TwitchRecover.Core.Enums.Timeout;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class holds all of the downloading methods
 * and handles all of the downloads.
 */
public class Download {
    private static final int MAX_TRIES=5;
    /**
     * This method downloads a file from a
     * given URL and downloads it at a given
     * file path.
     * @param url           String value representing the URL to download.
     * @param fp            String value representing the complete file path of the file.
     * @return String       String value representing the complete file path of where the file was downloaded.
     * @throws IOException
     */
    public static String download(String url, String fp) throws IOException {
        String extension=url.substring(url.lastIndexOf("."));
        URL dURL=new URL(url);
        File dFile=new File(fp+extension);
        FileUtils.copyURLToFile(dURL, dFile, Timeout.CONNECT.time, Timeout.READ.time);
        return dFile.getAbsolutePath();
    }

    public static String m3u8Download(String url, String fp) throws IOException {
        return m3u8Download(url, fp, null);
    }

    /**
     * Same as {@link #m3u8Download(String, String)} but reports progress
     * (completed vs total segments) to the given listener.
     * @param url       String value representing the M3U8 playlist URL.
     * @param fp        String value representing the complete output file path.
     * @param listener  Progress listener (may be null).
     * @return String   Complete file path of the merged output file.
     */
    public static String m3u8Download(String url, String fp, ProgressListener listener) throws IOException {
        FileHandler.createTempFolder();
        ArrayList<String> chunks=M3U8Handler.getChunks(url);
        NavigableMap<Integer, File> segmentMap=TSDownload(chunks, listener);
        return FileHandler.mergeFile(segmentMap, fp);
    }

    /**
     * This method creates a temporary download
     * from a URL.
     * @param url       URL of the file to be downloaded.
     * @return File     File object of the file that will be downloaded and is returned.
     * @throws IOException
     */
    public static File tempDownload(String url) throws IOException{
        URL dURL=new URL(url);
        String prefix=FilenameUtils.getBaseName(dURL.getPath());
        if(prefix.length()<2){     //This has to be implemented since the prefix value of the createTempFile method
            prefix="00"+prefix;     //which we use to create a temp file, has to be a minimum of 3 characters long.
        }
        else if(prefix.length()<3){
            prefix="0"+prefix;
        }
        File downloadedFile;
        if(FileHandler.TEMP_FOLDER_PATH==null){     //If no folder path has already being declared then to just store the file in the general temp folder.
            downloadedFile=File.createTempFile(prefix+"-", "."+FilenameUtils.getExtension(dURL.getPath()));    //Creates the temp file.
        }
        else {
            downloadedFile = File.createTempFile(prefix + "-", "." + FilenameUtils.getExtension(dURL.getPath()), new File(FileHandler.TEMP_FOLDER_PATH + File.separator));    //Creates the temp file.
        }
        downloadedFile.deleteOnExit();
        FileUtils.copyURLToFile(dURL, downloadedFile, Timeout.CONNECT.time, Timeout.READ.time);
        return downloadedFile;
    }

    /**
     * This method downloads all of the segments
     * of an M3U8 playlist and incorporates them all
     * in a navigable map.
     * @param links                         Arraylist holding all of the links to download.
     * @return NavigableMap<Integer, File>  Navigable map holdding the index and file objects of each TS segment.
     */
    private static NavigableMap<Integer, File> TSDownload(ArrayList<String> links, final ProgressListener listener){
        NavigableMap<Integer, File> segmentMap=new TreeMap<>();
        Queue<String> downloadQueue=new ConcurrentLinkedQueue<>();
        for(String link: links){    //Adds all the links to the ressource queue.
            downloadQueue.offer(link);
        }
        final int total=links.size();
        final AtomicInteger completed=new AtomicInteger(0);
        int index=0;
        ThreadPoolExecutor downloadTPE=(ThreadPoolExecutor) Executors.newFixedThreadPool(100);
        while(!downloadQueue.isEmpty()){
            index++;
            String item=downloadQueue.poll();
            final int finalIndex=index;
            final String finalItem=item;
            downloadTPE.execute(new Runnable() {
                int currentTries=1;
                @Override
                public void run() {
                    boolean done=false;
                    while(currentTries<=MAX_TRIES && !done) {
                        try {
                            File tempTS = tempDownload(finalItem);
                            segmentMap.put(finalIndex, tempTS);
                            done=true;
                        }
                        catch(Exception ignored) {
                            currentTries++;     //Avoid an infinite retry loop on a persistently failing segment.
                        }
                    }
                    int c=completed.incrementAndGet();
                    if(listener!=null){
                        listener.update(c, total);
                    }
                }
            });
        }
        downloadTPE.shutdown();
        try{
            downloadTPE.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        catch(Exception e){
            Thread.currentThread().interrupt();
        }
        return segmentMap;
    }
}