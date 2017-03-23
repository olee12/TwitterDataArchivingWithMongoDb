package twitterdataanalysis;

import com.mongodb.BasicDBObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import twitter4j.FilterQuery;
import twitter4j.HashtagEntity;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;
import twitter4j.UserMentionEntity;
import static twitterdataanalysis.TweetSearch.isTableViewReady;

public class TweetStream {
    MainJFrame par;
    public static int maxNumberOfStream;
    public static int currentNumberOfStream;
    public static JTable streamTweetInputViewTable = null;
    public static boolean isTableViewReady = false;
    public String streamTags;
    int counter = 0;
    public void streamQuery(TwitterStream twitterStream, String args, int maxNumberofTweet, JTable streamTweetInputViewTable,MainJFrame par) {
        this.par = par;
        TweetStream.streamTweetInputViewTable = streamTweetInputViewTable;
        System.out.println("Called and null ? " + TweetStream.streamTweetInputViewTable.toString());
        streamTags = args;
        new Thread() {
            @Override
            public void run() {
                try {
                    myFilterStream(twitterStream, args, maxNumberofTweet, TweetStream.streamTweetInputViewTable);
                    
                } catch (TwitterException ex) {
                    Logger.getLogger(TweetStream.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
        System.out.println("Stream Thread Complete.");

    }

    public void myFilterStream(TwitterStream twitterStream, String args, int maxNumberofTweet, JTable searchTweetInputViewTable) throws TwitterException {

        StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {

                //System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
                //String rawJSON = TwitterObjectFactory.getRawJSON(status);
                //String fileName = "statuses/111111" + status.getId()+ ".json";
                //try {
                //  SaveRawJSON.storeJSON(rawJSON, fileName);
                //} catch (IOException ex) {
                ///   Logger.getLogger(TweetStream.class.getName()).log(Level.SEVERE, null, ex);
                // }
                saveStreamStatusToDataBase(status, TweetStream.streamTweetInputViewTable);
                TweetStream.currentNumberOfStream+=1;
                if(TweetStream.currentNumberOfStream>=TweetStream.maxNumberOfStream) return ;

            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };

        twitterStream.addListener(listener);

        ArrayList<Long> follow = new ArrayList<Long>();
        ArrayList<String> track = new ArrayList<String>();
        
            if (isNumericalArgument(args)) {
                for (String id : args.split(",")) {
                    follow.add(Long.parseLong(id));
                }
            } else {
                track.addAll(Arrays.asList(args.split(",")));
            }
        

        long[] followArray = new long[follow.size()];
        for (int i = 0; i < follow.size(); i++) {
            followArray[i] = follow.get(i);
        }
        String[] trackArray = track.toArray(new String[track.size()]);

        // filter() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
        twitterStream.filter(new FilterQuery(0, followArray, trackArray));
    }

    public static boolean isNumericalArgument(String argument) {
        String args[] = argument.split(",");
        boolean isNumericalArgument = true;
        for (String arg : args) {
            try {
                Integer.parseInt(arg);
            } catch (NumberFormatException nfe) {
                isNumericalArgument = false;
                break;
            }
        }
        return isNumericalArgument;
    }

    public void saveStreamStatusToDataBase(Status tweet, JTable streamTable) {
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
        basicObj.put("search_tag", this.streamTags);
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
        basicObj.put("search_tag", this.streamTags);
        updateSearchTweetInputViewTable(streamTable, basicObj);
    }

    synchronized public void updateSearchTweetInputViewTable(JTable searchTweetInputViewTable, BasicDBObject bObject) {

        DefaultTableModel defaultTableModel = (DefaultTableModel) searchTweetInputViewTable.getModel();
        
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
