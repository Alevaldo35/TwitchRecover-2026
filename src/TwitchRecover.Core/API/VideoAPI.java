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

package TwitchRecover.Core.API;

import TwitchRecover.Core.Compute;
import TwitchRecover.Core.Enums.FileExtension;
import TwitchRecover.Core.Enums.Quality;
import TwitchRecover.Core.Feeds;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This class handles all
 * of the API methods directly
 * related to VODs.
 */
public class VideoAPI {
    /**
     * This method gets the list of feeds
     * of a VOD that is still up from the
     * VOD ID.
     * This is NOT to be used for sub-only VODs.
     * @param VODID     Long value representing the VOD ID.
     * @return Feeds    Feeds object holding the list of VOD feeds and their corresponding qualities.
     */
    public static Feeds getVODFeeds(long VODID){
        String[] auth=getVODToken(VODID);  //0: Token; 1: Signature.
        if(auth==null){
            return new Feeds();
        }
        return API.getPlaylist("https://usher.ttvnw.net/vod/"+VODID+".m3u8?sig="+auth[1]+"&token="+auth[0]+"&allow_source=true&player=twitchweb&allow_spectre=true&allow_audio_only=true");
    }

    /**
     * This method retrieves the M3U8 feeds for
     * sub-only VODs by utilising values provided
     * in the public VOD metadata API.
     * @param VODID     Long value representing the VOD ID to retrieve the feeds for.
     * @return Feeds    Feeds object holding all of the feed URLs and their respective qualities.
     */
    public static Feeds getSubVODFeeds(long VODID, Boolean highlight){
        Feeds feeds=new Feeds();
        //Get the JSON response of the VOD via the Twitch GQL API.
        //(The old Kraken v5 endpoint - api.twitch.tv/kraken/videos - was shut down by Twitch.)
        String response="";
        try{
            CloseableHttpClient httpClient= HttpClients.createDefault();
            HttpPost httppost=new HttpPost("https://gql.twitch.tv/gql");
            httppost.addHeader("Content-Type", "text/plain;charset=UTF-8");
            httppost.addHeader("Client-ID", "kimne78kx3ncx6brgo4mv6wki5h1ko");
            String query="{\"operationName\":\"VideoPlayer_VODSeekbarPreviewVideo\",\"variables\":{\"includePrivate\":false,\"videoID\":\""+VODID+"\"},\"extensions\":{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"07e99e4d56c5a7c67117a154777b0baf85a5ffefa393b213f4bc712ccaf85dd6\"}}}";
            httppost.setEntity(new StringEntity(query));
            CloseableHttpResponse httpResponse=httpClient.execute(httppost);
            if(httpResponse.getStatusLine().getStatusCode()==200){
                BufferedReader br=new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                String line;
                while ((line = br.readLine()) != null) {
                    response+=line;
                }
                br.close();
            }
            httpResponse.close();
            httpClient.close();
        }
        catch (Exception ignored){}
        //Parse the JSON response. Bail out gracefully if the VOD could not be found.
        if(response.isEmpty()){
            return feeds;
        }
        JSONObject jO=new JSONObject(response);
        if(jO.isNull("data") || jO.getJSONObject("data").isNull("video")){
            return feeds;
        }
        String seekPreviewsURL=jO.getJSONObject("data").getJSONObject("video").optString("seekPreviewsURL", "");
        //The seek previews URL already contains the actual CDN host and path
        //(https://<host>/<vodPath>/storyboards/<id>-info.json), so derive the
        //M3U8 base directly from it instead of fuzzing the (often outdated)
        //list of known Twitch VOD domains.
        String domain= Compute.singleRegex("(https:\\/\\/.+\\/)storyboards\\/[0-9]*-info\\.json",seekPreviewsURL);
        if(domain==null){
            return feeds;
        }
        String[] auth=getVODToken(VODID);
        if(auth==null){
            return feeds;
        }
        String token=auth[0];
        JSONObject jo = new JSONObject(token);
        JSONArray restricted = jo.getJSONObject("chansub").getJSONArray("restricted_bitrates");
        if(highlight){
            for(int i=0;i<restricted.length();i++){
                feeds.addEntry(domain+restricted.get(i).toString()+"/highlight-"+VODID+FileExtension.M3U8.fileExtension, Quality.getQualityV(restricted.get(i).toString()));
            }
        }
        else {
            for(int i = 0; i < restricted.length(); i++) {
                feeds.addEntry(domain + restricted.get(i).toString() + "/index-dvr" + FileExtension.M3U8.fileExtension, Quality.getQualityV(restricted.get(i).toString()));
            }
        }
        return feeds;
    }

    /**
     * This method retrieves the
     * token and signature values
     * for a VOD.
     * @param VODID     Long value representing the VOD ID to get the token and signature for.
     * @return String[] String array holding the token in the first position and the signature in the second position.
     * String[2]: 0: Token; 1: Signature.
     */
    private static String[] getVODToken(long VODID){
        return API.getToken(String.valueOf(VODID), true);
    }
}