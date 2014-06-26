/******************************************************************
 * File:        Updater.java
 * Created by:  Dave Reynolds
 * Created on:  26 Jun 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.registry.utility;

import java.io.ByteArrayOutputStream;

import javax.ws.rs.core.MediaType;

import com.hp.hpl.jena.rdf.model.Resource;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;

/**
 * Helper class to send updates to a registry instance.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Updater {
    protected static final String MEDIA_TYPE = "text/turtle";
    
    ApacheHttpClient client;
    String registry;
    String webapp;

    
    public Updater(String registry) {
        this.registry = registry;
        
        webapp = registry.replaceFirst("http://.*?/(.*/)", "$1");
    }

    /**
     * Map a data URI to the corresponding URI for the registry, which may be hosted as a different root (e.g. localhost)
     */
    public String localize(String uri) {
        return registry + uri.replaceFirst("http://.*?/(.*)", "$1").replaceFirst(webapp, "");
    }
    
    /**
     * Replace an entity in the target registry with the new description given here
     */
    protected boolean update(Resource entity) {
        String target = localize( entity.getURI() );
        
        System.out.println("Updating " + target);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        entity.getModel().write(out, "Turtle");
        
        WebResource r = client.resource(target);
        ClientResponse response = r.type(MEDIA_TYPE).put(ClientResponse.class, out.toString());
        if (response.getStatus() >= 400) {
            System.out.println("Failed: " + response.getEntity(String.class)+ " (" + response.getStatus() + ")");
            return false;
        }
        return true;
    }
    
    /**
     * Delete an entity in the target registry
     */
    protected boolean delete(String entity) {
        String target = localize(entity);
        System.out.println("Deleting: " + target);
        
        WebResource r = client.resource(target);
        ClientResponse response = r.delete(ClientResponse.class);
        if (response.getStatus() >= 400) {
            System.out.println("Failed: " + response.getEntity(String.class)+ " (" + response.getStatus() + ")");
            return false;
        }
        return true;
    }
    
    public void login(String userid, String key) {
        System.out.println("Login " + registry + "system/security/apilogin");
        client = ApacheHttpClient.create();
        client.getProperties().put(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, true);
        
        Form loginform = new Form();
        loginform.add("userid", userid);
        loginform.add("password", key);
        
        WebResource r = client.resource(registry + "system/security/apilogin");
        ClientResponse response = r.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class, loginform);
        if (response.getStatus() >= 400) {
            System.err.println("Login failed: " + response.getEntity(String.class)+ " (" + response.getStatus() + ")");
            System.exit(1);
        }
    }
    
    public void logout() {
        System.out.println("Logout");
    }
    
}
