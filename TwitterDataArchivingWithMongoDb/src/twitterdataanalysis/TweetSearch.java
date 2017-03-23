/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package twitterdataanalysis;

import com.mongodb.BasicDBObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import twitter4j.HashtagEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserMentionEntity;

/**
 *
 * @author Tahmidolee
 */
public class TweetSearch {
    MainJFrame par;
    public static boolean isTableViewReady = false;
    public String searchTag;
    public void searchQuery(String myquery, Twitter twitter, int maxNumberofTweet, JTable searchTweetInputViewTable,MainJFrame par) throws TwitterException {
        this.par = par;
        System.out.println("Searching for : " + myquery);
        this.searchTag = myquery;
        new Thread() {
            @Override
            public void run() {
                try {
                    searchAndSaveToDatabase(myquery, twitter, maxNumberofTweet, searchTweetInputViewTable);
                    
                } catch (TwitterException ex) {
                    JOptionPane.showConfirmDialog(null, "Exception in search thread " + ex);
                    Logger.getLogger(TweetSearch.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }

    public void searchAndSaveToDatabase(String myquery, Twitter twitter, int maxNumberofTweet, JTable searchTweetInputViewTable) throws TwitterException {
        Query query = new Query(myquery);
        query.setCount(maxNumberofTweet);
        QueryResult result;
        do {
            result = twitter.search(query);
            List<Status> tweets = result.getTweets();
            for (Status tweet : tweets) {
                //  System.out.println("new tweet @" + tweet.getUser().getScreenName() + " -- " + tweet.getText()+" -- "+tweet.getRetweetCount());
                // HashSet<String> users = new HashSet<>();
                // String sss = new String(tweet.getUser().getScreenName());
                // System.out.println("Name : "+tweet.getUser().getScreenName());
                // users.add(sss);
                // String rawJSON = TwitterObjectFactory.getRawJSON(tweet);
                // MongoDBJDBC.insertJsonItemToCollection(rawJSON, MongoDBJDBC.collectionForSearchTweet);
                //String fileName = "statuses/" + tweet.getId() + ".json";
                // System.out.println("Total Users : "+users.size());
                BasicDBObject basicObj = new BasicDBObject();
                basicObj.put("user_name", tweet.getUser().getScreenName());
                basicObj.put("user_screen_name", tweet.getUser().getName());
                basicObj.put("user_id", tweet.getUser().getId());
                basicObj.put("user_created_at", tweet.getUser().getCreatedAt().getTime());
                basicObj.put("user_description", tweet.getUser().getDescription());
                basicObj.put("user_lang", tweet.getUser().getLang());
                basicObj.put("user_url", tweet.getUser().getURL());

                basicObj.put("user_location", tweet.getUser().getLocation());
                basicObj.put("user_friends", tweet.getUser().getFriendsCount());
                basicObj.put("user_followers", tweet.getUser().getFollowersCount());
                basicObj.put("user_time_zone", tweet.getUser().getTimeZone());
                basicObj.put("user_utc_offset", tweet.getUser().getUtcOffset());
                basicObj.put("user_is_varified", tweet.getUser().isVerified());
                BasicDBObject userBasicObj = new BasicDBObject(basicObj);
                MongoDBJDBC.insertBasicDBObjectToCollection(userBasicObj, MongoDBJDBC.collectionForUuser);
                basicObj.put("tweet_ID", tweet.getId());
                basicObj.put("retweet_count", tweet.getRetweetCount());
                basicObj.put("favorite_count", tweet.getFavoriteCount());
                basicObj.put("source", tweet.getSource());
                // basicObj.put("coordinates", tweet.getGeoLocation().toString());

                UserMentionEntity[] mentioned = tweet.getUserMentionEntities();

                ArrayList<String> mentionedUsers = new ArrayList<>();
                for (UserMentionEntity user : mentioned) {
                    mentionedUsers.add(user.getScreenName());
                }

                basicObj.put("tweet_mentioned_users", mentionedUsers);
                basicObj.put("tweet_mentioned_count", mentioned.length);

                HashtagEntity[] hashTags = tweet.getHashtagEntities();
                ArrayList<String> userHashTags = new ArrayList<>();
                for (HashtagEntity hash : hashTags) {
                    userHashTags.add("#" + hash.getText());
                }
                basicObj.put("tweet_hashtags", userHashTags);
                basicObj.put("tweet_hashtab_count", hashTags.length);

                basicObj.put("tweet_text", tweet.getText());
                basicObj.put("created_at", tweet.getCreatedAt().getTime());
                basicObj.put("is_retweet", tweet.isRetweet());
                basicObj.put("is_retweeted", tweet.isRetweeted());
                 basicObj.put("search_tag", this.searchTag);
                // basicObj.put("tweet_country", tweet.getPlace().getCountry().toString() == null ? "null":tweet.getPlace().getCountry() == null);
                //basicObj.put("tweet_country_code", tweet.getPlace());
                //System.out.println("----"+basicObj.toString());
                MongoDBJDBC.insertBasicDBObjectToCollection(basicObj, MongoDBJDBC.collectionForSearchTweet);
                makeBasicDBObjectForTableView(tweet, searchTweetInputViewTable);
            }

        } while ((query = result.nextQuery()) != null);
    }

    public void makeBasicDBObjectForTableView(Status tweet, JTable searchTweetInputViewTable) {

        BasicDBObject basicObj = new BasicDBObject();
        basicObj.put("user_id", tweet.getUser().getId());
        basicObj.put("user_name", tweet.getUser().getScreenName());
        basicObj.put("user_screen_name", tweet.getUser().getName());
        basicObj.put("user_location", tweet.getUser().getLocation());
        basicObj.put("tweet_ID", tweet.getId());
        basicObj.put("tweet_text", tweet.getText());
        basicObj.put("retweet_count", tweet.getRetweetCount());
        basicObj.put("favorite_count", tweet.getFavoriteCount());
        UserMentionEntity[] mentioned = tweet.getUserMentionEntities();
        basicObj.put("tweet_mentioned_count", mentioned.length);
        basicObj.put("search_tag", this.searchTag);
        ArrayList<String> mentionedUsers = new ArrayList<>();
        for (UserMentionEntity user : mentioned) {
            mentionedUsers.add(user.getScreenName());
        }

        basicObj.put("tweet_mentioned_users", mentionedUsers);

        HashtagEntity[] hashTags = tweet.getHashtagEntities();
        ArrayList<String> userHashTags = new ArrayList<>();
        for (HashtagEntity hash : hashTags) {
            userHashTags.add("#" + hash.getText());
        }
        basicObj.put("tweet_hashtags", userHashTags);
        basicObj.put("tweet_hashtab_count", hashTags.length);

        basicObj.put("created_at", tweet.getCreatedAt());
        basicObj.put("is_retweet", tweet.isRetweet());
        basicObj.put("is_retweeted", tweet.isRetweeted());
        updateSearchTweetInputViewTable(searchTweetInputViewTable, basicObj);
    }

    synchronized public void updateSearchTweetInputViewTable(JTable searchTweetInputViewTable, BasicDBObject bObject) {

        DefaultTableModel defaultTableModel = (DefaultTableModel) searchTweetInputViewTable.getModel();
        searchTweetInputViewTable.setEnabled(false);
        if (isTableViewReady == false) {
            defaultTableModel.setColumnCount(bObject.size());
            Vector<Object> colVec = new Vector<>();
            for (int i = 0; i < bObject.toMap().keySet().toArray().length; i++) {
                colVec.add(bObject.toMap().keySet().toArray()[i]);
            }

            defaultTableModel.setColumnIdentifiers(colVec);
            isTableViewReady = true;
        }
        Vector<Object> rowVec = new Vector<>();
        for (int i = 0; i < bObject.toMap().values().toArray().length; i++) {
            rowVec.add(bObject.toMap().values().toArray()[i]);
        }

        defaultTableModel.addRow(rowVec);
    }

    private Object toMap() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
