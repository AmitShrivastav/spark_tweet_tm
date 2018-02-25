/**
 * Created by Kmar.Amit on 20-09-2017.
 */
package com.tmobile.commerce;


import com.tmobile.commerce.NLP.TwitterLangProcessing;
import com.tmobile.commerce.Util.TwitterAuthenticateHelper;
import org.apache.spark.api.java.JavaPairRDD;
import org.springframework.context.annotation.ComponentScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.spark.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.*;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.twitter.*;
import twitter4j.Status;
import scala.Tuple2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;


@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan({"com.tmobile.commerce"})
    public class TweetApplication {

    private static final Logger logger = LoggerFactory.getLogger(TweetApplication.class);



        public static void main(String[] args) {

            TwitterLangProcessing  twitterLangProcessing = new TwitterLangProcessing();
            SpringApplication.run(TweetApplication.class, args);
          //  TweetApplication tw = new TweetApplication();

            /* final String consumerKey = "Rt2h9BWFlfVOiGSKoMQUfkwJx";
            final String consumerSecret = "inb9MVgLiKceVH8hoey5NQQsvB4ccfBeerBhcfugrY5ofHJAPq";
            final String accessToken = "441117375-hkXOUU5Hsay8cdUv4Z5uuN0Ck4nKnOiRmfsKaxAq";
            final String accessTokenSecret = "kDLsGX1QJ3bcVK0dKLKvxaZC5JfTxgKc8UvDJF3D7cnEj";*/

            SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("SparkTwitterHelloWorldExample");
            JavaStreamingContext jssc = new JavaStreamingContext(conf, new Duration(2000));
            new TwitterAuthenticateHelper().configureTwitterCredentials();

            String[] filters={"T-mobile","iphonex"};

            JavaReceiverInputDStream<Status> twitterStream = TwitterUtils.createStream(jssc,filters);
            connection();

            // Without filter: Output text of all tweets
            JavaDStream<String> statuses = twitterStream.map(
                    new Function<Status, String>() {
                        public String call(Status status) {

                            String text = status.getText();
                            twitterLangProcessing.getNLPResponse(text);
                            return text;
                        }
                    }
            );

            /**
             * Get the stream of hashtags from the stream of tweets: To get the hashtags from the status string,
             * we need to identify only those words in the message that start with “#”. This can be done as follows:
             * The flatMap operation applies a one-to-many operation to each record in a DStream and then flattens the
             * records to create a new DStream. In this case, each status string is split by space to produce a
             * DStream where each record is a word. Then we apply the filter function to retain only the hashtags.
             * The resulting hashtags DStream is a stream of RDDs having only the hashtags. If you want to see the result,
             * add hashtags.print() and try running the program. You should see something like this (assuming no other DStream has print on it):
             */
            JavaDStream<String> words = statuses.flatMap(
                    new FlatMapFunction<String, String>() {
                        public Iterable<String> call(String in) {
                            return Arrays.asList(in.split(" "));
                        }
                    }
            );

            JavaDStream<String> hashTags = words.filter(
                    new Function<String, Boolean>() {
                        public Boolean call(String word) { return word.startsWith("#"); }
                    }
            );


            /**
             * Count the hashtags over a 5 minute window: Next, we’d like to count these hashtags over a 5 minute moving window.
             * A simple way to do this would be to gather together the last 5 minutes of data and process it in the usual map-reduce way
             * — map each tag to a (tag, 1) key-value pair and then reduce by adding the counts. However, in this case, counting over a
             * sliding window can be done more intelligently. As the window moves, the counts of the new data can be added to the previous window’s counts,
             * and the counts of the old data that falls out of the window can be ‘subtracted’ from the previous window’s counts. This can be done using DStreams as follows:
             *
             * There are two functions that are being defined for adding and subtracting the counts. new Duration(60 * 5 * 1000)
             * specifies the window size and new Duration(1 * 1000) specifies the movement of the window.

             Note that only ‘invertible’ reduce operations that have ‘inverse’ functions (like how subtraction is the inverse of
             addition) can be optimized in this manner. The generated counts DStream will have records that are (hashtag, count) tuples
             . If you print counts and run this program, you should see something like this:
             */
         /*   JavaPairDStream<String, Integer> tuples = hashTags.mapToPair(
                    new PairFunction<String, String, Integer>() {
                        public Tuple2<String, Integer> call(String in) {
                            return new Tuple2<String, Integer>(in, 1);
                        }
                    }
            );

            JavaPairDStream<String, Integer> counts = tuples.reduceByKeyAndWindow(
                    new Function2<Integer, Integer, Integer>() {
                        public Integer call(Integer i1, Integer i2) { return i1 + i2; }
                    },
                    new Function2<Integer, Integer, Integer>() {
                        public Integer call(Integer i1, Integer i2) { return i1 - i2; }
                    },
                    new Duration(60 * 5 * 1000),
                    new Duration(1 * 1000)
            );

    */
            /*Find the top 10 hashtags based on their counts: Finally, these counts have to be used to find the popular hashtags.
            A simple (but not the most efficient) way to do this is to sort the hashtags based on their counts and take the top 10 records.
            Since this requires sorting by the counts, the count (i.e., the second item in the (hashtag, count) tuple) needs to be made the key. Hence, we need to
            first use a map to flip the tuple and then sort the hashtags. Finally, we need to get the top 10 hashtags and print them. All this can be done as follows:
             */

          /*  JavaPairDStream<Integer, String> swappedCounts = counts.mapToPair(
                    new PairFunction<Tuple2<String, Integer>, Integer, String>() {
                        public Tuple2<Integer, String> call(Tuple2<String, Integer> in) {
                            return in.swap();
                        }
                    }
            );

            JavaPairDStream<Integer, String> sortedCounts = swappedCounts.transformToPair(
                    new Function<JavaPairRDD<Integer, String>, JavaPairRDD<Integer, String>>() {
                        public JavaPairRDD<Integer, String> call(JavaPairRDD<Integer, String> in) throws Exception {
                            return in.sortByKey(false);
                        }
                    });

            sortedCounts.foreach(
                    new Function<JavaPairRDD<Integer, String>, Void> () {
                        public Void call(JavaPairRDD<Integer, String> rdd) {
                            String out = "\nTop 10 hashtags:\n";

                            for (Tuple2<Integer, String> t: rdd.take(10)) {
                                out = out + t.toString() + "\n";
                            }
                            System.out.println(out);
                            return null;
                        }
                    }
            );  */
            // With filter: Only use tweets with geolocation and print location+text.
        /*JavaDStream<Status> tweetsWithLocation = twitterStream.filter(
                new Function<Status, Boolean>() {
                    public Boolean call(Status status){
                        if (status.getGeoLocation() != null) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
        );
        JavaDStream<String> statuses = tweetsWithLocation.map(
                new Function<Status, String>() {
                    public String call(Status status) {
                        return status.getGeoLocation().toString() + ": " + status.getText();
                    }
                }
        );*/


           statuses.print();
            System.out.print("----");
            hashTags.print();
        //    String text = "This World is an bad place";

            jssc.start();
        }


        public static void connection()  {
            try {
                String dbURL = "jdbc:redshift://tmobile-test.c2ssg6uroxtx.us-east-1.redshift.amazonaws.com:5439/tmobiledb";
                String MasterUsername = "testadmin";
                String MasterUserPassword = "Atestadmin1";
                Class.forName("com.amazon.redshift.jdbc41.Driver");
                Connection conn = null;
                Statement stmt = null;
                //Open a connection and define properties.
                System.out.println("Connecting to database...");
                Properties props = new Properties();

                //Uncomment the following line if using a keystore.
                //props.setProperty("ssl", "true");
                props.setProperty("user", MasterUsername);
                props.setProperty("password", MasterUserPassword);
                conn = DriverManager.getConnection(dbURL, props);

                System.out.println("Listing system tables...");
                stmt = conn.createStatement();
                String sql;
                sql = "select * from public.demoTmobtest;";
                ResultSet rs = stmt.executeQuery(sql);

                //Get the data from the result set.
                while (rs.next()) {
                    //Retrieve two columns.
                    String catalog = rs.getString("tmobiltweetid");
                    String name = rs.getString("tshow");

                    //Display values.
                    System.out.print("00000000000000000000000Catalog: " + catalog);
                    System.out.println(", Name: " + name);
                }
                rs.close();
                stmt.close();
                conn.close();
            }catch(Exception e)
            {
                    e.printStackTrace();
            }
        }


    }

