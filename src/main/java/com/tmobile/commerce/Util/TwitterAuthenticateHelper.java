package com.tmobile.commerce.Util;

import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;

/**
 * Created by Kmar.Amit on 01-10-2017.
 */
public class TwitterAuthenticateHelper implements Serializable {

    @Value("${twitter.consumerKey}")
    private  String consumerKey;

    @Value("${twitter.consumerSecret}")
    private  String consumerSecret;

    @Value("${twitter.accessToken}")
    private  String accessToken;

    @Value("${twitter.accessTokenSecret}")
    protected  String accessTokenSecret;

    public void configureTwitterCredentials()
    {
        System.setProperty("twitter4j.oauth.consumerKey", "Rt2h9BWFlfVOiGSKoMQUfkwJx");
        System.setProperty("twitter4j.oauth.consumerSecret", "inb9MVgLiKceVH8hoey5NQQsvB4ccfBeerBhcfugrY5ofHJAPq");
        System.setProperty("twitter4j.oauth.accessToken", "441117375-hkXOUU5Hsay8cdUv4Z5uuN0Ck4nKnOiRmfsKaxAq");
        System.setProperty("twitter4j.oauth.accessTokenSecret", "kDLsGX1QJ3bcVK0dKLKvxaZC5JfTxgKc8UvDJF3D7cnEj");
    }
}
