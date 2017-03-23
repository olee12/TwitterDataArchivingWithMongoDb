/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package twitterdataanalysis;
import com.mongodb.MongoClient;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import com.mongodb.util.JSON;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.swing.JOptionPane;
import org.bson.Document;


/**
 *
 * @author Tahmidolee
 */
public class MongoDBJDBC {
    public static DB db;
   
    public static MongoClient mongoClient;
    public static DBCollection collectionForSearchTweet;
    public static DBCollection collectionForUuser;
    public static DBObject dBObject;
    public static MongoCollection<Document> collectionForTwitterDocumentcollection;
    public static MongoCollection<Document> collectionForTwitterUsers;
    public static MongoDatabase database;
    public static Vector<String> allUserNames = new Vector<>();
    public static Vector<Long> allTweetIds = new Vector<>();
    public static Vector<String> allSearchTags = new Vector<>();
    public static int counter = 0;
    public static MainJFrame par = null;
    public static void connectToDB() {
	
      try{
         // To connect to mongodb server
         mongoClient = new MongoClient( "localhost" , 27017 );
			
         // Now connect to your databases
         db = mongoClient.getDB("mytwitter");
         collectionForSearchTweet = db.getCollection("search_tweet");
         collectionForUuser = db.getCollection("twitter_user");
         database = mongoClient.getDatabase("mytwitter");
        
         collectionForTwitterDocumentcollection = (MongoCollection<Document>) database.getCollection("search_tweet");
         System.out.println("Connect to database successfully");
         
      }catch(Exception e){
          JOptionPane.showMessageDialog(par,"Start the server and restart the App.");
       //  System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      }
      
   }
   
   synchronized public static void insertJsonItemToCollection(String content, DBCollection collec){
       BasicDBObject res = (BasicDBObject)
                JSON.parse(content.toString());
       collec.insert(res);
   }
   
   synchronized public static void insertBasicDBObjectToCollection(BasicDBObject dbObject, DBCollection collec){
       collec.insert(dbObject);
      // if(counter%50==0) par.updateLocalFieldsInfo();
   }
   synchronized public static void updateCollection(BasicDBObject query,BasicDBObject upd ,DBCollection collec){
    //  DBObject ob =   collec.findOne(query);
     // if(ob!=null){
    //      System.out.println("updStatus : "+collec.updateMulti(ob, upd));
    //  }
      // if(counter%50==0) par.updateLocalFieldsInfo(); 
      collec.update(query, upd);
   }
   synchronized public static BasicDBObject getSingleItemFromCollection(BasicDBObject query,DBCollection collec){
    //  DBObject ob =   collec.findOne(query);
     // if(ob!=null){
    //      System.out.println("updStatus : "+collec.updateMulti(ob, upd));
    //  }
      // if(counter%50==0) par.updateLocalFieldsInfo(); 
      return (BasicDBObject) collec.findOne(query);
   }
   synchronized public static void setAllUserNamesTweetIds(){
       allUserNames.clear();
       Set<String> users = new HashSet<>();
       Set<String> searchTags = new HashSet<>();
       Set<Long> tweetIds = new HashSet<>();
       MongoCursor<Document> cursor = collectionForTwitterDocumentcollection.find().projection(fields(include("user_name"),include("tweet_ID"),include("search_tag"),excludeId())).iterator();
        
        while (cursor.hasNext()) {
           Document next = cursor.next();
           users.add((String) next.get("user_name"));
           tweetIds.add((Long) next.get("tweet_ID"));
           searchTags.add((String) next.get("search_tag"));
       }
        allUserNames = new Vector<>(users);
        allTweetIds = new Vector<>(tweetIds);
        allSearchTags = new Vector<>(searchTags);
   }
   public static void closeConnection(){
       mongoClient.close();;
   }
}
