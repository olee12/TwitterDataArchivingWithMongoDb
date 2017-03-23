/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package twitterdataanalysis;

/**
 *
 * @author Tahmidolee
 */

import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import twitter4j.HashtagEntity;
import twitter4j.UserMentionEntity;
import static twitterdataanalysis.TweetStream.isTableViewReady;

public class GetUserTimeline {
   public MainJFrame par;
    public String userName;
    public JTable viewTable;
   public static boolean isTableViewReady = false;
    public void getUserTimeLine(String userName, Twitter twitter, int maxNumberofTweet, JTable viewTable,MainJFrame par){
        this.par = par;
        this.userName = userName;
        this.viewTable = viewTable;
        new Thread() {
            @Override
            public void run() {
                getTimeLine(userName,maxNumberofTweet, twitter, viewTable);
            }
        }.start();
    }
    
    public void getTimeLine(String userName,int maxStatus,Twitter twitter,JTable viewTable) {
        // gets Twitter instance with default credentials
        
        try {
            List<Status> statuses;
            String user;
            
                user = userName;
                statuses = twitter.getUserTimeline(user);
            int counter = 1;
            System.out.println("Showing @" + user + "'s user timeline. "+statuses.size());
            for (Status status : statuses) {
                saveUserStatusToDataBase(status, viewTable);
                System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
                counter+=1;
                if(counter>maxStatus) return;
            }
           //par.updateLocalFieldsInfo();
        } catch (TwitterException te) {
           // JOptionPane.showMessageDialog(viewTable,"Failed to get timeline: " + te.getMessage() );
            System.out.println("Failed to get timeline: " + te.getMessage());
            
        }
    }
    public void saveUserStatusToDataBase(Status tweet, JTable streamTable) {
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
        basicObj.put("search_tag", this.userName);
        // basicObj.put("tweet_country", tweet.getPlace().getCountry().toString() == null ? "null":tweet.getPlace().getCountry() == null);
        //basicObj.put("tweet_country_code", tweet.getPlace());
        //System.out.println("----"+basicObj.toString());
        MongoDBJDBC.insertBasicDBObjectToCollection(basicObj, MongoDBJDBC.collectionForSearchTweet);
        makeBasicDBObjectForTableView(tweet, streamTable);
    }

    public void makeBasicDBObjectForTableView(Status tweet, JTable streamTable) {

       BasicDBObject basicObj = new BasicDBObject();
        basicObj.put("user_id", tweet.getUser().getId());
        basicObj.put("user_name", tweet.getUser().getScreenName());
        basicObj.put("user_screen_name", tweet.getUser().getName());
        basicObj.put("tweet_ID", tweet.getId());
        basicObj.put("tweet_text", tweet.getText());
        basicObj.put("retweet_count", tweet.getRetweetCount());
        basicObj.put("favorite_count", tweet.getFavoriteCount());
        UserMentionEntity[] mentioned = tweet.getUserMentionEntities();
        basicObj.put("tweet_mentioned_count", mentioned.length);

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
        basicObj.put("search_tag", this.userName);
        updateUserStatusInputViewTable(streamTable, basicObj);
    }

    synchronized public void updateUserStatusInputViewTable(JTable searchTweetInputViewTable, BasicDBObject bObject) {

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
}